 
package sqw.certificat;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Logger;


public class Sign {

    private static final Logger logger= Logger.getLogger(Sign.class);
    private boolean _hasCertificare = false;
    private boolean _expired = false;
    private String _algorithm = null;
    private boolean _strictAlgorithm = false;
    private static String newLine = System.getProperty("line.separator");
    private String _pkcs11config = "";
    private String _library = null;
    private boolean _isSlot = false;
    private Certificate[] _chain;
    private PrivateKey _privateKey;
    Provider _etpkcs11 = null;
    CertAlias _certAlias = null;
    private String _configPath = null;

    private Class _p11Class = null;
    private Object _p11 = null;

    public CertAlias getCertAlias() {
        return _certAlias;
    }

    public Certificate[] getChain() {
        return _chain;

    }

    public PrivateKey getPrivateKey() {
        return _privateKey;
    }

    public Sign() {
    }

    public void setNoCertificate() {
        _hasCertificare = false;
        releaseToken();
    }

    public String initSign(String inputPin, String cfgFile, CertificateChooser chooser, String algorithm) {
        _pkcs11config = "";
        _algorithm = null;
        _strictAlgorithm = false;
        _library = null;
        _expired = false;
        _isSlot = false;
        BufferedReader cfg = null;
        String line = null;
        try {
            cfg = new BufferedReader(new FileReader(cfgFile));
            do {
                line = cfg.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }
                if (parts[0].trim().equals("library")) {
                    _library = parts[1].trim();
                } else if (parts[0].trim().equals("slotListIndex")
                        || parts[0].trim().equals("slot")) {
                    _isSlot = true;
                }
                if (parts[0].trim().equals("algorithm")) {
                    if (parts[1].trim().startsWith("!")) {
                        _algorithm = parts[1].trim().substring(1);
                        _strictAlgorithm = true;
                    } else {
                        _algorithm = parts[1].trim();
                    }
                } else {
                    _pkcs11config += line + newLine;
                }
            } while (true);
        } catch (Throwable ex) {
            logger.error("err1: ", ex);
            return "eroare fisier configurare: " + ex.getMessage();
        } finally {
            if (cfg != null) {
                try {
                    cfg.close();
                } catch (IOException ex) {
                    logger.error("err2: ", ex);
                    return "eroare inchidere fisier configurare: " + ex.getMessage();
                }
            }
        }
        if (_library == null) {
            return "fisierul de configurare nu contine atributul 'library'";
        }
        if (_algorithm == null) {
            //alegere mai judicioasa!!!
            if (algorithm != null) {
                _algorithm = algorithm;
            } else {
                _algorithm = "sunpkcs11";
            }
        }
        if (_algorithm.equals("sunpkcs11")) {
            //In mod empiric am constatat ca, pe token-urile care au
            //  certificate reinnoite CertSign sub acelasi alias, se selecteaza
            //  aleatoriu, cand certificatul valid, cand cel expirat.
            //Incercam sa prindem, facand mai multe incercari,
            //  certificatul valid
            String err = null;
            for (int i = 0; i < 10; i++) {
                _expired = false;
                err = initSunpkcs11(inputPin, cfgFile, chooser);
                if (_expired == false) {
                    return err;
                }
                _isSlot = true;
            }
            return err;
        } else if (_algorithm.equals("mscapi")) {
            return initMscapi(inputPin, cfgFile, chooser);
        } else if (_algorithm.equals("p12")) {
            return initP12(inputPin);
        }
//        else if(_algorithm.equals("dll"))
//        {
//            return initDll(inputPin, cfgFile, chooser);
//        }
        return "algoritm semnare necunoscut. Corectati in fisierul " + cfgFile + " valoarea atributului 'algorithm'";
    }

    private String initSunpkcs11(String inputPin, String cfgFile, CertificateChooser chooser) {
        KeyStore.PasswordProtection pin = null;
        X509Certificate cert = null;
        String text = null;
        //compune tagul slot
        if (_isSlot == false) {
            long[] slots = null;
//            CK_SLOT_INFO info = null;
            try {
                //urmatoarele 4 instructiuni, precum si instructiunea mai indepartata
                //  -->  _etpkcs11 = new sun.security.pkcs11.SunPKCS11(configStream);
                //  au fost executate folosind java reflection, pt. a putea folosi
                //  codul si cu java 64 biti (care nu are pachetul pkcs11)
//                CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
                Class initArgsClass = Class.forName("sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS");
                Object initArgs = initArgsClass.getConstructor().newInstance();
//                initArgs.flags = PKCS11Constants.CKF_OS_LOCKING_OK;
                Field fld = initArgsClass.getDeclaredField("flags");
                fld.setLong(initArgs,
                        Class.forName("sun.security.pkcs11.wrapper.PKCS11Constants").getField("CKF_OS_LOCKING_OK").getLong(null));
//                PKCS11 p11 = PKCS11.getInstance(_library, "C_GetFunctionList", initArgs, false);
                _p11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
                Method mth = _p11Class.getMethod("getInstance", String.class, String.class, initArgsClass, boolean.class);
                _p11 = mth.invoke(null, _library, "C_GetFunctionList", initArgs, false);
//                slots = p11.C_GetSlotList(true);
                mth = _p11Class.getMethod("C_GetSlotList", boolean.class);
                slots = (long[]) mth.invoke(_p11, true);
//                info = p11.C_GetSlotInfo(slots[0]);
                if (slots != null && slots.length > 0) {
//                    _pkcs11config += "slotListIndex=" + slots[0] + newLine;
                    _pkcs11config += "slot=" + slots[0] + newLine;
                }
            } catch (Throwable t) {
                logger.error("err3: ", t);
                return "eroare acces driver: " + _library + " (Corectati parametrul library din fisierul dist\\config\\SMART_CARD.cfg astfel incat sa indice calea reala pe calculatorul dumneavoastra catre driverul corespunzator SMART_CARD-ului folosit)" + newLine + "       (" + t + ")";
            }
        }
        try {
            // connect to eToken PKCS#11 provider
            byte[] pkcs11configBytes = _pkcs11config.getBytes();
            ByteArrayInputStream configStream = new ByteArrayInputStream(pkcs11configBytes);
            //blocaj cu driverul aladdin 2013-06-06:
//            _etpkcs11 = new sun.security.pkcs11.SunPKCS11(configStream);
            Constructor ct = Class.forName("sun.security.pkcs11.SunPKCS11").getConstructor(InputStream.class);
            _etpkcs11 = (Provider) ct.newInstance(configStream);
            Security.addProvider(_etpkcs11);
            // get user PIN
            pin = new KeyStore.PasswordProtection(inputPin.toCharArray());
            // create key store builder
            KeyStore.Builder keyStoreBuilder = KeyStore.Builder.newInstance("PKCS11", _etpkcs11, pin);
            // create key store
            //blocaj cu driverul aladdin 2013-06-06:
            KeyStore keyStore = keyStoreBuilder.getKeyStore();
            String alias = null;
            String error = "certificatul nu a putut fi detectat";
            int cnt = 0, flag = 0;
            Enumeration e = keyStore.aliases();
            List coll = new ArrayList();
            do {
                cnt++;
                alias = String.valueOf(e.nextElement());
                if (keyStore.isKeyEntry(alias) == true) {
                    cert = (X509Certificate) keyStore.getCertificate(alias);
                    try {
                        cert.checkValidity();
                        coll.add(new CertAlias(alias, cert));
//                            break;
                    } catch (CertificateExpiredException ex) {
                        error = "Certificat expirat: " + ex.toString();
                        _expired = true;
                        flag |= 1;
                    } catch (CertificateNotYetValidException ex) {
                        error = "Certificat nu este inca valid: " + ex.toString();
                        flag |= 2;
                    } catch (Throwable ex) {
                        logger.error("err4: ", ex);
                        error = "Certificat eronat: " + ex.toString();
                        //  logError(30, ex);
                        flag |= 4;
                    }
//                            StringBuffer bf = new StringBuffer();
//                            bf.append(chooser._newLine + "Alias certificat:----------------------------------------------------" + chooser._newLine);
//                            bf.append(alias);
//                            bf.append(chooser._newLine + "Certificat----------------------------------------------------" + chooser._newLine);
//                            bf.append(cert);
//                            bf.append(chooser._newLine + "Private key: ----------------------------------------------------" + chooser._newLine);
//                            bf.append(keyStore.getKey(alias, null));
//                            bf.append(chooser._newLine + "Sfarsit certificat----------------------------------------------------" + chooser._newLine);
//                            chooser.insertMessage(bf.toString());
                }
            } while (e.hasMoreElements());
            if (coll.size() > 1) {
                _certAlias = chooser.chooseCertificate(coll);
            } else if (coll.size() == 0) {
                if (cnt > 1 && flag != 1 && flag != 2 && flag != 4) {
                    error = "Certificatele sunt sau expirate sau nu sunt inca valide sau eronate";
                }
                return error;
            } else {
                _certAlias = (CertAlias) coll.get(0);
            }
            cert = _certAlias._cert;
            alias = _certAlias._alias;
            _expired = false;
            _privateKey = (PrivateKey) keyStore.getKey(alias, null);
            _chain = null;
            _chain = keyStore.getCertificateChain(alias);
            _chain[0] = cert;
        } catch (ProviderException ex) {
            logger.error("err5: ", ex);
            //  logError(1, ex);
            if (ex.getMessage().equals("Initialization failed")) {
//                return ex.toString() + " (Probabil aveti un alt tip de SmartCard conectat. Deconectati alte tipuri de SmartCarduri (daca exista) si folositi optiunea \"*autoDetect\")";
                return analizaEroare(ex) + " (Probabil aveti un alt tip de SmartCard conectat. Deconectati alte tipuri de SmartCarduri (daca exista) si folositi optiunea \"*autoDetect\")";
            } else if (ex.getMessage().equals("Error parsing configuration")) {
//                return ex.toString() + " (Calea catre driverul SmartCardului (care se afla inscrisa in fisierul .cfg corespunzator acestuia) contine unul din urmatoarele caractere: \"~()\". Solutie: Copiati continutul intregului folder in alta locatie si modificati corespunzator calea din fisierul .cfg. (vezi si http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6581254))";
                return analizaEroare(ex) + " (Calea catre driverul SmartCardului (care se afla inscrisa in fisierul .cfg corespunzator acestuia) contine unul din urmatoarele caractere: \"~()\". Solutie: Copiati continutul intregului folder in alta locatie si modificati corespunzator calea din fisierul .cfg. (vezi si http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6581254))";
            }
//            return ex.toString();
            return analizaEroare(ex);
        } catch (KeyStoreException ex) {
            // logError(2, ex);
            logger.error("err6: ", ex);
            if (ex.getMessage().equals("KeyStore instantiation failed")) {
//                return ex.toString() + " (Probabil nu aveti nici un SmartCard conectat sau PIN-ul nu este corect sau PIN blocat datorita depasirii numarului de login esuate sau, daca SmartCardul este Schlumberger, introduceti doar primele 8 caractere ale PIN-ului)";
                return analizaEroare(ex) + " (Probabil nu aveti nici un SmartCard conectat sau PIN-ul nu este corect sau PIN blocat datorita depasirii numarului de login esuate sau, daca SmartCardul este Schlumberger, introduceti doar primele 8 caractere ale PIN-ului)";
            }
            return ex.toString();
        } catch (NoSuchAlgorithmException ex) {
            logger.error("err7: ", ex);
            // logError(3, ex);
            return ex.toString();
        } catch (UnrecoverableKeyException ex) {
            logger.error("err8: ", ex);
            // logError(4, ex);
            return ex.toString();
        } catch (Throwable ex) {
            logger.error("err9: ", ex);
            //logError(5, ex);
            return ex.toString();
        }
        return null;
    }

    private String analizaEroare(Throwable ex) {
        String text = ex.toString();
        do {
            try {
                ex = ex.getCause();
                if (ex == null) {
                    return text;
                }
                text = ex.toString();
            } catch (Throwable ex1) {
                logger.error("err10: ", ex1);
                return text;
            }
        } while (true);
    }

    private String initMscapi(String inputPin, String cfgFile, CertificateChooser chooser) {
        X509Certificate cert = null;
        //compune tagul slot
        try {
            //add provider
            _etpkcs11 = (Provider) Class.forName("sun.security.mscapi.SunMSCAPI").newInstance();
//            _etpkcs11 = new sun.security.mscapi.SunMSCAPI();
            Security.addProvider(_etpkcs11);
            // create key store
            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, inputPin.toCharArray());
            fixAliases(keyStore);
            String alias = null;
            String error = "certificatul nu a putut fi detectat";
            int cnt = 0, flag = 0;
            Enumeration e = keyStore.aliases();
            List coll = new ArrayList();
            do {
                cnt++;
                alias = String.valueOf(e.nextElement());
                if (keyStore.isKeyEntry(alias) == true) {
                    cert = (X509Certificate) keyStore.getCertificate(alias);
                    try {
                        cert.checkValidity();
                        coll.add(new CertAlias(alias, cert));
//                            break;
                    } catch (CertificateExpiredException ex) {
                        error = "Certificat expirat: " + ex.toString();
                        _expired = true;
                        flag |= 1;
                    } catch (CertificateNotYetValidException ex) {
                        error = "Certificat nu este inca valid: " + ex.toString();
                        flag |= 2;
                    } catch (Throwable ex) {
                        error = "Certificat eronat: " + ex.toString();
                        // logError(30, ex);
                        flag |= 4;
                    }
//                            StringBuffer bf = new StringBuffer();
//                            bf.append(chooser._newLine + "Alias certificat:----------------------------------------------------" + chooser._newLine);
//                            bf.append(alias);
//                            bf.append(chooser._newLine + "Certificat----------------------------------------------------" + chooser._newLine);
//                            bf.append(cert);
//                            bf.append(chooser._newLine + "Private key: ----------------------------------------------------" + chooser._newLine);
//                            bf.append(keyStore.getKey(alias, null));
//                            bf.append(chooser._newLine + "Sfarsit certificat----------------------------------------------------" + chooser._newLine);
//                            chooser.insertMessage(bf.toString());
                }
            } while (e.hasMoreElements());
            if (coll.size() > 1) {
                _certAlias = chooser.chooseCertificate(coll);
            } else if (coll.size() == 0) {
                if (cnt > 1 && flag != 1 && flag != 2 && flag != 4) {
                    error = "Certificatele sunt sau expirate sau nu sunt inca valide sau eronate";
                }
                return error;
            } else {
                _certAlias = (CertAlias) coll.get(0);
            }
            cert = _certAlias._cert;
            alias = _certAlias._alias;
            _expired = false;

            _privateKey = (PrivateKey) keyStore.getKey(alias, inputPin.toCharArray());
            _chain = null;
            _chain = keyStore.getCertificateChain(alias);
            _chain[0] = cert;
        } catch (ProviderException ex) {
            // logError(10, ex);
            logger.error("err11: ", ex);
            if (ex.getMessage().equals("Initialization failed")) {
                return ex.toString() + " (Probabil aveti un alt tip de SmartCard conectat. Deconectati alte tipuri de SmartCarduri (daca exista) si folositi optiunea \"*autoDetect\")";
            } else if (ex.getMessage().equals("Error parsing configuration")) {
                return ex.toString() + " (Calea catre driverul SmartCardului (care se afla inscrisa in fisierul .cfg corespunzator acestuia) contine unul din urmatoarele caractere: \"~()\". Solutie: Copiati continutul intregului folder in alta locatie si modificati corespunzator calea din fisierul .cfg. (vezi si http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6581254))";
            }
            return ex.toString();
        } catch (KeyStoreException ex) {
            logger.error("err12: ", ex);
            //  logError(11, ex);
            if (ex.getMessage().equals("KeyStore instantiation failed")) {
                return ex.toString() + " (Probabil nu aveti nici un SmartCard conectat sau PIN-ul nu este corect sau, daca SmartCardul este Schlumberger, introduceti doar primele 8 caractere ale PIN-ului)";
            }
            return ex.toString();
        } catch (NoSuchAlgorithmException ex) {
            logger.error("err13: ", ex);
            //  logError(12, ex);
            return ex.toString();
        } catch (UnrecoverableKeyException ex) {
            logger.error("err14: ", ex);
            // logError(13, ex);
            return ex.toString();
        } catch (Throwable ex) {
            logger.error("err15: ", ex);
            //  logError(14, ex);
            return ex.toString();
        }
        return null;
    }

    //workaround pt. cazul certificatelor reinnoite cu acelasi alias (cu cel precedent)
    //modifica aliasurile in obiectul keyStore pt. a le face unice
    private static void fixAliases(KeyStore keyStore) {
        Field field;
        KeyStoreSpi keyStoreVeritable;

        try {
            field = keyStore.getClass().getDeclaredField("keyStoreSpi");
            field.setAccessible(true);
            keyStoreVeritable = (KeyStoreSpi) field.get(keyStore);

            if ("sun.security.mscapi.KeyStore$MY".equals(keyStoreVeritable.getClass().getName())) {
                Collection entries;
                String alias, hashCode;
                X509Certificate[] certificates;

                field = keyStoreVeritable.getClass().getEnclosingClass().getDeclaredField("entries");
                field.setAccessible(true);
                entries = (Collection) field.get(keyStoreVeritable);

                for (Object entry : entries) {
                    field = entry.getClass().getDeclaredField("certChain");
                    field.setAccessible(true);
                    certificates = (X509Certificate[]) field.get(entry);

                    hashCode = Integer.toString(certificates[0].hashCode());

                    field = entry.getClass().getDeclaredField("alias");
                    field.setAccessible(true);
                    alias = (String) field.get(entry);

                    if (!alias.equals(hashCode)) {
                        field.set(entry, alias.concat(" - ").concat(hashCode));
                    } // if
                } // for
            } // if
        } catch (Exception exception) {
            logger.error("err16: ", exception);
        }
    }

    private byte[] streamToByteArray(InputStream is) throws IOException {

        byte[] buff = new byte[512];
        int read = -1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((read = is.read(buff)) >= 0) {

            bos.write(buff, 0, read);
        }
        bos.close();
        return bos.toByteArray();
    }

    public void releaseToken() {
        try {
            for (Provider p : Security.getProviders()) {
                if (p.getName().contains("SunPKCS11")) {
                    Security.removeProvider(p.getName());
                }
            }
            if (_p11Class != null && _p11 != null) {
                Method mth = _p11Class.getMethod("finalize", null);
                mth.invoke(_p11);
            }
            _p11Class = null;
            _p11 = null;
            Thread.sleep(1000);
        } catch (Throwable ex) {
            logger.error("err17: ", ex);
        }
    }

    private String initP12(String inputPin) {
        KeyStore ks = null;
        String alias = null;
        try {
            // citire certificat
            ks = KeyStore.getInstance("pkcs12");
            _library = new File(new File(_configPath).getCanonicalPath(),
                    _library).getCanonicalPath();
            ks.load(new FileInputStream(_library),
                    inputPin.toCharArray());
            alias = (String) ks.aliases().nextElement();
            _privateKey = (PrivateKey) ks.getKey(alias, inputPin.toCharArray());
            _chain = ks.getCertificateChain(alias);
        } catch (Throwable e) {
            logger.error("err18: ", e);
            return "eroare semnare cu certificat '" + _library
                    + "': " + e.toString();
        }
        return null;
    }

    public class CertAlias {

        public String _alias;
        public X509Certificate _cert;

        public CertAlias(String alias, X509Certificate cert) {
            _alias = alias;
            _cert = cert;
        }

        @Override
        public String toString() {
            return _alias;// + _cert.getIssuerDN().getName();
        }
    }
}

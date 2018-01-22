/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqw.apelspv;

import java.security.cert.X509Certificate;
import java.util.List;
import sqw.certificat.CertificateChooser;
import sqw.certificat.Sign.CertAlias;

public class CertificateChooserImpl implements CertificateChooser {
//se alege intotdeauna primul certificat de pe token
    //trebuie modificat daca se doreste o alta forma de alegere

    int _index = 0;

    public CertificateChooserImpl(int index) {
        _index = index;
    }

    public CertAlias chooseCertificate(List col) {
        return (CertAlias) col.get(_index);
    }

//nu e cazul
    public String chooseZipFile(String xmlFile, int zipOption) {

        return null;
    }
}

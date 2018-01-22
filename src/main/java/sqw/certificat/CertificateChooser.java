
package sqw.certificat;


import java.util.List;
import sqw.certificat.Sign.CertAlias;


public interface CertificateChooser
{
    public CertAlias chooseCertificate(List col);
    public String chooseZipFile(String xmlFile, int zipOption);
}

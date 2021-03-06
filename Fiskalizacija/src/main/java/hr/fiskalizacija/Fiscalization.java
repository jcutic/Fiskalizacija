package hr.fiskalizacija;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.xml.soap.SOAPMessage;
import hr.model.CertParameters;

/**
 * Klasa koja je su�elje prema korisniku, kreiranjem objekta ove klase mogu se
 * koristiti sve funkcionalmnosti librarya
 * Prije pokretanja metoda za prijavu poslovnog prostora potrebno je postaviti:
 * 	- 
 * 
 * Prije fiskaliziranja ra�una, potrebno je postaviti:
 * 	-
 *  
 */
public class Fiscalization extends CertParameters{

	
	/**
	 * Konstruktor za kreiranje objekta klase Fiscalization kada jo� nije 
	 * kreiran .jks keystore ili ako unutar .jks keystorea ima samo jedan alias
	 * za par privatni klju� - certifikat
	 * 
	 * */
	public Fiscalization(String filePath, String fileName, String password) {
		super(filePath, fileName, password);
	}

	
	/**
	 * Konstruktor za kreiranje objekta klase Fiscalization kada ima vi�e 
	 * aliasa za par privatni klju� - certifikat .jks
	 * Potrebno je definirati alias s kojim �e se privatnim klju�em potpisivati
	 * 
	 * @param filePath
	 * @param fileName
	 * @param password
	 * @param aliasForCertInJKS
	 */
	public Fiscalization(String filePath, String fileName, String password, String aliasForCertInJKS) {
		super(filePath, fileName, password, aliasForCertInJKS);
	}
	
	
	/**
	 * Metoda za konverziju PFX keystorea i SSL certifikata za komunikaciju u JKS keystore
	 * Poziva se samo jednom, prije potpisivanja SOAP poruke
	 * Za defautno kori�tenje potrebno je postaviti da oba certifikata budu istog naziva (npr. FiskalCert.cer i FiskalCert.pfx) 
	 * i u istom direktoriju (imaju istu putanju)
	 * Defaultne vrijednosti (password, putanja, naziv) �e biti iste kao i prilikom kreiranje objekta   
	 * Ako nemaju istu putanju ili naziv, mogu�e je postaviti sve vrijednosti setterima nakon kreiranja objekta klase Fiskalization.
	 *
	 * @param fiskal
	 * @return broj unesenih certifikata u JKS keystore
	 */
	public static int convertFromPKCSAndSSLToJKS(Fiscalization fiskal){
		return PrepareCertificate.convertFromPKCSAndSSLToJKS(fiskal);
	}
	
	
	/**
	 * Slalje Echo poruke prema web servisu porezne uprave
	 * Echo poruka provjerava da li je mogu�e uspostaviti komunikaciju s web servisom
	 * 
	 * Vra�a odgovor web servisa u obliku stringa
	 * 
	 * @param fiskal
	 * @return
	 */
	public String sendEchoMessage(Fiscalization fiskal){
		return writeSoap(new Connections().sendSoapMessage(new CreateXmls().createEchoMessage(), this));
	}
	
	
	/**
	 * Prijava poslovnog prostora, potrebno je kreirati objekte klasa koje su potrebne za kreiranje XML-a
	 * Metoda:
	 * 	- kreira XML iz podataka unesenih u objekte klase
	 * 	- kreira SOAP poruku
	 * 	- potpisuje SOAP poruku
	 * 	- �alje SOAP poruku prema web servisu porezne uprave 
	
	 * @param fiskal
	 * @param objectForCreateXml
	 * @return odgovor od web servisa porezne uprave kao string
	 */

	public SOAPMessage sendSoap(Fiscalization fiskal, Object objectForCreateXml){
		SOAPMessage soapMessage = new Connections().sendSoapMessage(new SignVerify().signSoap(new CreateXmls().createSoapMessage(new CreateXmls().createXmlForRequest(objectForCreateXml)), this), this);
		
		// Potrebno je preuzeti sve dijelove SOAP poruke u objekt klase koja za sada jo� nije kreirana s JAXB-em
		
		if(new SignVerify().verifyMessage(soapMessage)){
			System.out.println("SOAP poruka koju je poslao servis Porezne uprave je pravilno potpisana.");
		}else{
			System.out.println("Neispravan digitalni potpis.");
		}
		return  soapMessage;
	}
	
	/**
	 * Metoda za preuzimanje OIB-a iz certifikata 
	 * 
	 * @param fiskal
	 * @return
	 */
	public String getOIBFromCert(Fiscalization fiskal){
		String oib = null;
		try {
			KeyStore keyStoreJKS = KeyStore.getInstance(KEYSTORE_TYPE_JKS);
			keyStoreJKS.load(new FileInputStream(getPathOfJKSCert() + getNameOfJKSCert() + EXTENSION_OF_JKS), getPasswdOfJKSCert().toCharArray());
			oib = getOIBFromCert(keyStoreJKS, getAliasForPairJKSCert());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oib;
	}
}

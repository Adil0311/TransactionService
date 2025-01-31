package org.uniupo.it.mqttConfig;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class MqttOptions {

    private final MqttConnectOptions options;

    public MqttOptions() {
        options = new MqttConnectOptions();
        try {
            setUpOptions(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory getSocketFactory() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Load CA certificate
        InputStream caStream = MqttOptions.class.getClassLoader().getResourceAsStream("certificates/ca.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(caStream);

        // Load client certificate
        InputStream certStream = MqttOptions.class.getClassLoader().getResourceAsStream("certificates/client.crt");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);

        // Load client private key
        InputStream keyStream = MqttOptions.class.getClassLoader().getResourceAsStream("certificates/client.key");
        assert keyStream != null;
        PEMParser pemParser = new PEMParser(new InputStreamReader(keyStream));
        Object object = pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build("".toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        KeyPair key;
        if (object instanceof PEMEncryptedKeyPair) {
            System.out.println("Encrypted key - we will use provided password");
            key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
        } else if (object instanceof PrivateKeyInfo) {
            System.out.println("Unencrypted PrivateKeyInfo key - no password needed");
            key = converter.getKeyPair(convertPrivateKeyFromPKCS8ToPKCS1((PrivateKeyInfo) object));
        } else {
            System.out.println("Unencrypted key - no password needed");
            key = converter.getKeyPair((PEMKeyPair) object);
        }
        pemParser.close();

        // Set up CA certificate
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // Set up client certificate
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), "".toCharArray(), new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());

        // Create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    private static PEMKeyPair convertPrivateKeyFromPKCS8ToPKCS1(PrivateKeyInfo privateKeyInfo) throws Exception {
        // Parse the key wrapping to determine the internal key structure
        ASN1Encodable asn1PrivateKey = privateKeyInfo.parsePrivateKey();
        // Convert the parsed key to an RSA private key
        RSAPrivateKey keyStruct = RSAPrivateKey.getInstance(asn1PrivateKey);
        // Create the RSA public key from the modulus and exponent
        RSAPublicKey pubSpec = new RSAPublicKey(keyStruct.getModulus(), keyStruct.getPublicExponent());
        // Create an algorithm identifier for forming the key pair
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
        System.out.println("Converted private key from PKCS #8 to PKCS #1 RSA private key\n");
        // Create the key pair container
        return new PEMKeyPair(new SubjectPublicKeyInfo(algId, pubSpec), new PrivateKeyInfo(algId, keyStruct));
    }

    private void setUpOptions(MqttConnectOptions options) throws Exception {
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        SSLSocketFactory socketFactory = getSocketFactory();
        options.setSocketFactory(socketFactory);
    }

    public MqttConnectOptions getOptions() {
        return options;
    }
}
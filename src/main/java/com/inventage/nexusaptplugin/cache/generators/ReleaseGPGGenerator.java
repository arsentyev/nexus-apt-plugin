package com.inventage.nexusaptplugin.cache.generators;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;

import com.inventage.nexusaptplugin.cache.DebianFileManager;
import com.inventage.nexusaptplugin.cache.FileGenerator;
import com.inventage.nexusaptplugin.cache.RepositoryData;
import com.inventage.nexusaptplugin.sign.AptSigningConfiguration;
import com.inventage.nexusaptplugin.sign.PGPSigner;


public class ReleaseGPGGenerator implements FileGenerator {

    private final DebianFileManager fileManager;
    private final AptSigningConfiguration aptSigningConfiguration;

    @Inject
    public ReleaseGPGGenerator(DebianFileManager fileManager,
                               AptSigningConfiguration aptSigningConfiguration) {
        this.fileManager = fileManager;
        this.aptSigningConfiguration = aptSigningConfiguration;
    }

    @Override
    public byte[] generateFile(RepositoryData data)
            throws Exception {
        // Read Release
        byte[] release = fileManager.getFile("Release", data);

        // Get the key and sign the Release file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PGPSigner signer = this.aptSigningConfiguration.getSigner();
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(signer.getSecretKey().getPublicKey().getAlgorithm(), PGPUtil.SHA256));
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, signer.getPrivateKey());

        BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(baos));
        signatureGenerator.update(release);
        signatureGenerator.generate().encode(out);

        out.close();
        baos.close();

        return baos.toByteArray();
    }
}

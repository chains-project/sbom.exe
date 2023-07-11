package org.example;

import org.example.sbom.Cyclonedx;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Terminator {
    private static Options OPTIONS;

    private static final Map<String, String> classesToMD5Hash = new HashMap<>();

    // Use build time statistics to determine the hash of the class
    static {
        // Correct hash is acb694f66d6e4158ebf108d0f04338b1
        classesToMD5Hash.put("io/github/chains_project/cs/commons/Pair", "acb694f66d6e4158ebf108d0f04338b");
        classesToMD5Hash.put("foo/Main", "6d5ce21e2dc74790fe14ca3036cda67d");
    }


    public static void premain(String agentArgs, Instrumentation inst) {
        OPTIONS = new Options(agentArgs);
        inst.addTransformer(
                new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                        return terminationCode(className, classfileBuffer, protectionDomain);
                    }
                }
        );
    }

    private static byte[] terminationCode(String className, byte[] classfileBuffer, ProtectionDomain protectionDomain) {
        String jarPath;
        Cyclonedx parsedSbom = OPTIONS.getSbom();
        if (protectionDomain == null) {
            // we skip the bootstrap and platform classloader
            return classfileBuffer;
        } else {
            jarPath = protectionDomain.getCodeSource().getLocation().getPath();
            if (isHashOfClassCorrect(className, jarPath)) {
                return classfileBuffer;
            } else {
                System.err.printf("Class %s is not in the SBOM%n", className);
                System.exit(1);
            }
        }
        return null;
    }

    private static boolean isHashOfClassCorrect(String className, String jarPath) {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry classFileCandidate = enumEntries.nextElement();
                if (String.format("%s.class", className).equals(classFileCandidate.getName())) {
                    String md5 = computeMD5(jar.getInputStream(classFileCandidate));
                    return classesToMD5Hash.get(className).equals(md5);
                }
            }
        }
        catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    private static String computeMD5(InputStream byteStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = MessageDigest.getInstance("MD5").digest(byteStream.readAllBytes());
        return new BigInteger(1, hash).toString(16);
    }
}

// 1 go to all purls in the sbom and check if the class name is in the maven central jar
// 2 what would help me would be that I have all names of the classes in the sbom

package codesum.lm.main;

import codesum.lm.topicsum.Repository;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CodeSummarizationUtils {

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    private static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) >= 0) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static Map<String, Repository> repoNameParser(String[] projects) {
        Map<String, Repository> repositoryList = new HashMap<String, Repository>();
        for (int i = 0; i < projects.length; i++) {
            String[] repoFields = projects[i].split("~");
            Repository repository = new Repository();
            repository.setLogin(repoFields[1]);
            repository.setRepoName(repoFields[2]);
            repository.setRepoId(Long.parseLong(repoFields[3]));
            if (repoFields.length >= 6) {
                repository.setBranch(repoFields[6]);
            } else {
                repository.setBranch("master");
            }
            repositoryList.put(repository.getRepoName() + "-" + repository.getBranch(), repository);
        }
        return repositoryList;
    }

    public static String[] getZipProjectList(String projectsDir) {
        final File projDir = new File(projectsDir);
        return projDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.lastIndexOf('.') > 0) {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex);
                    if (str.equals(".zip")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public static void unzipProjects(List<String> projectsList, String projectsDir, String unzipPath) throws IOException {
        for (String project : projectsList) {
            unzip(projectsDir + "/" + project, unzipPath);
        }
    }

    public static String[] getUnzipProjectList(File unzipProjDir) {
        return unzipProjDir.list(new FilenameFilter() {
            @Override
            public boolean accept(final File current, final String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    public static void saveAsTextFile(StringBuilder stringBuilder, String desPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(desPath + "topicModelJson.txt")))) {
            bw.append(stringBuilder);
            bw.flush();
        } catch (IOException ioe) {
            System.err.print("Error ocurred while writting json to file");
            ioe.printStackTrace();
        }
    }
}

package codesum.lm.tui;

import codesum.lm.main.CodeSummarizationUtils;
import codesum.lm.topicsum.GibbsSampler;
import codesum.lm.topicsum.Repository;
import codesum.lm.topicsum.TopicModel;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CodeSummarization {

    /**
     * Command line parameters
     */
    public static class Parameters {

        @Parameter(names = {"-w", "--workingDir"}, description = "Working directory where the topic model creates necessary files", required = true)
        String workingDir;

        @Parameter(names = {"-d", "--projectsDir"}, description = "Directory containing project subdirectories", required = true)
        String projectsDir;

        @Parameter(names = {"-i", "--iterations"}, description = "Number of iterations for the topic model")
        int iterations = 1000;

        @Parameter(names = {"-c", "--ratio"}, description = "Desired compression percentage in term of (important file*100/all files)", required = true)
        int compressionRatio;

        @Parameter(names = {"-b", "--backoffTopic"}, description = "Background topic to back off to (0-2)", validateWith = FoldSourceFile.checkBackoffTopic.class)
        int backoffTopic = 2;

        @Parameter(names = {"-t", "--ignoreTests"}, description = "Whether to ignore test classes")
        Boolean ignoreTestFiles = true;

        @Parameter(names = {"-bs", "--batchSize"}, description = "batch size of iterations")
        int batchSize = 20;

        @Parameter(names = {"-o", "--destination"}, description = "generated json to be stored", required = true)
        String outputFilePath;
    }

    public static void main(final String[] args) throws Exception {

        final Parameters params = new Parameters();
        final JCommander jc = new JCommander(params);

        try {
            jc.parse(args);
            codeSummarization(params.workingDir, params.projectsDir,
                    params.iterations, params.compressionRatio,
                    params.backoffTopic, params.ignoreTestFiles, params.batchSize, params.outputFilePath);
        } catch (final ParameterException e) {
            System.out.println(e.getMessage());
            jc.usage();
        }

    }

    private static void codeSummarization(final String workingDir,
                                          final String projectsDir,
                                          final int iterations,
                                          final int compressionRatio,
                                          final int backOffTopic,
                                          final boolean ignoreTestFiles, int batchSize, String outputFilePath) throws Exception {
        final String ESHEADER = "{ \"index\" : { \"_index\" : \"repotopic\", \"_type\" : \"typerepotopic\" } }";
        final String[] projectsZip = CodeSummarizationUtils.getZipProjectList(projectsDir);
        Map<String, Repository> repoNameVsRepoProperty = CodeSummarizationUtils.repoNameParser(projectsZip);
        List<String> projectZipList = Arrays.asList(projectsZip);
        Collections.shuffle(projectZipList); // to shuffle project list so that we get random order of projects in the list
        List<List<String>> projectsZipLists = Lists.partition(projectZipList, batchSize);
        Gson gson = new Gson();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 1;
        for (List<String> projectsList : projectsZipLists) {
            CodeSummarizationUtils.unzipProjects(projectsList, projectsDir);
            // Get all projects in projects directory
            final File unzipProjDir = new File(projectsDir + "_unzip");
            final String[] projects = CodeSummarizationUtils.getUnzipProjectList(unzipProjDir);
            System.out.println("Batch" + count);
            System.out.println("==========================");
            count++;
            GibbsSampler gibbsSampler = TrainTopicModel.trainTopicModel(workingDir, projectsDir + "_unzip",
                    projects, iterations);
            System.out.println("Size of the projects for file listing is  " + gibbsSampler.getCorpus().getProjects().length);
            for (String project : gibbsSampler.getCorpus().getProjects()) {
                TopicModel topicModel = new TopicModel();
                topicModel.setRepository(repoNameVsRepoProperty.get(project));
                ListSalientFiles.listSalientFiles(projectsDir + "_unzip", project, compressionRatio,
                        backOffTopic, gibbsSampler, ignoreTestFiles, topicModel);
                stringBuilder.append(ESHEADER);
                stringBuilder.append("\n");
                stringBuilder.append(gson.toJson(topicModel));
                stringBuilder.append("\n");
            }
            FileUtils.deleteDirectory(unzipProjDir);
        }
        CodeSummarizationUtils.saveAsTextFile(stringBuilder, outputFilePath);
    }
}

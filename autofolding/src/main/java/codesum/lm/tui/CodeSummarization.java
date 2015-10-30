package codesum.lm.tui;

import codesum.lm.main.CodeSummarizationUtils;
import codesum.lm.main.CodeUtils;
import codesum.lm.main.Settings;
import codesum.lm.topicsum.GibbsSampler;
import codesum.lm.topicsum.Repository;
import codesum.lm.topicsum.TopicModel;
import codesum.lm.topicsum.TopicSum;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keerathj on 28/10/15.
 */
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
    }

    public static void main(final String[] args) throws Exception {

        final Parameters params = new Parameters();
        final JCommander jc = new JCommander(params);

        try {
            jc.parse(args);
            codeSummarization(params.workingDir, params.projectsDir,
                    params.iterations, params.compressionRatio,
                    params.backoffTopic, params.ignoreTestFiles);
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
                                          final boolean ignoreTestFiles) throws Exception {


        final String[] projectsZip = CodeSummarizationUtils.getZipProjectList(projectsDir);
        Map<String, Repository> repoNameVsRepoProperty = CodeSummarizationUtils.repoNameParser(projectsZip);
        List<List<String>> projectsZipLists = Lists.partition(Arrays.asList(projectsZip), 10);
        List<TopicModel> topicModelList = new ArrayList<TopicModel>();
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
            for (String projectPath : gibbsSampler.getCorpus().getProjects()) {
                topicModelList.add(ListSalientFiles.listSalientFiles(projectsDir + "_unzip", projectPath, compressionRatio,
                        backOffTopic, gibbsSampler, ignoreTestFiles));
            }
            FileUtils.deleteDirectory(unzipProjDir);
        }
    }

}

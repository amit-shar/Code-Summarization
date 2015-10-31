package codesum.lm.tui;

import codesum.lm.main.CodeUtils;
import codesum.lm.main.Settings;
import codesum.lm.topicsum.GibbsSampler;
import codesum.lm.topicsum.TopicSum;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TrainTopicModel {
    /**
     * Train topic model for source code autofolding.
     * <p>
     * <p>
     * Serialized trained model saved in
     * workingDir/TopicSum/Source/SamplerState.ser
     *
     * @param workingDir  working directory where the topic model creates necessary
     *                    files
     * @param projectsDir directory containing project subdirectories
     * @param projects
     * @param iterations  number of iterations for the topic model
     */
    public static GibbsSampler trainTopicModel(final String workingDir,
                                               final String projectsDir, String[] projects,
                                               final int iterations) throws Exception {

        System.out
                .println("TASSAL: Tree-based Autofolding Software Summarization Algorithm");
        System.out
                .println("===============================================================");
        System.out.println("\nRunning topic model training stage...");

        // Get all projects in projects directory


        // Set paths and default code folder settings
        final Settings set = new Settings(workingDir, projectsDir, projects);

        // Create topic model base files in workingDir/TopicSum/Source/
        List<String> remove = CodeUtils.saveFileTokensByNodeID(set);
        //remove projects which do not have any java files
        if (remove.size() > 0) {
            List<String> project = new LinkedList<String>(Arrays.asList(projects));
            project.removeAll(remove);
            projects = project.toArray(new String[project.size()]);
        }
        // Train topic model and serialize model to
        // workingDir/TopicSum/Source/SamplerState.ser
        GibbsSampler gibbsSampler = TopicSum.trainTopicSum(workingDir + "TopicSum/Source/", projects,
                "SamplerState.ser", iterations);

        // Delete temporary directories
        final File dir = new File(workingDir + "TopicSum/Source/");
        for (final File file : dir.listFiles()) {
            if (!file.getName().contains("SamplerState.ser"))
                FileUtils.deleteDirectory(file);
        }
        return gibbsSampler;
    }

    private TrainTopicModel() {
    }

}
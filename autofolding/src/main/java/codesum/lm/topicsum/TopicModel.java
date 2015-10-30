package codesum.lm.topicsum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amits on 30/10/15.
 */
public class TopicModel {
    private List<Term> termList;
    private List<SalientFile> fileList;
    private String projectName;

    public TopicModel(String project) {
        this.projectName = project;
        this.termList = new ArrayList<Term>();
        this.fileList = new ArrayList<SalientFile>();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<SalientFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<SalientFile> fileList) {
        this.fileList = fileList;
    }

    public List<Term> getTermList() {
        return termList;
    }

    public void setTermList(List<Term> termList) {
        this.termList = termList;
    }
}

package codesum.lm.topicsum;

public class Repository {
    private String repoName;
    private Long repoId;
    private String repoStars;
    private boolean fork = false;
    private String login;
    private String branch;
    private String language = "Java";

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public Long getRepoId() {
        return repoId;
    }

    public void setRepoId(Long repoId) {
        this.repoId = repoId;
    }

    public String getRepoStars() {
        return repoStars;
    }

    public void setRepoStars(String repoStars) {
        this.repoStars = repoStars;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

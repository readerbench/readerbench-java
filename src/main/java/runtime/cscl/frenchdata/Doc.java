package runtime.cscl.frenchdata;

/**
 * Created by Dorinela on 5/9/2017.
 */
public class Doc {

    private String game;

    private Integer score;

    private String content;

    public Doc(String game, Integer score, String content) {
        this.game = game;
        this.score = score;
        this.content = content;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Doc{" +
                "game='" + game + '\'' +
                ", score=" + score +
                ", content='" + content + '\'' +
                '}';
    }
}



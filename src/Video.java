import com.oocourse.spec2.main.VideoInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Video implements VideoInterface {

    private final int id;
    private final int uploaderId;
    private final String type;
    private int playCount;
    private int likes;
    private int forwardCount;
    private int coins;
    private final List<Integer> commentIds;
    private final List<String> commentContents;

    public Video(int id, int uploaderId, String type) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.type = type;
        this.playCount = 0;
        this.likes = 0;
        this.forwardCount = 0;
        this.coins = 0;
        this.commentIds = new ArrayList<>();
        this.commentContents = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getUploaderId() {
        return uploaderId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getPlayCount() {
        return playCount;
    }

    @Override
    public int getLikes() {
        return likes;
    }

    @Override
    public int getForwardCount() {
        return forwardCount;
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public double getHeat() {
        return playCount + likes * 1.5 + forwardCount * 2.0 + coins * 2.5;
    }

    @Override
    public boolean containsComment(int commentId) {
        for (int cid : commentIds) {
            if (cid == commentId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof VideoInterface) {
            return ((VideoInterface) obj).getId() == id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int[] getCommentIds() {
        int[] copy = new int[commentIds.size()];
        for (int i = 0; i < commentIds.size(); i++) {
            copy[i] = commentIds.get(i);
        }
        return copy;
    }

    public String[] getCommentContents() {
        return commentContents.toArray(new String[0]);
    }

    void addPlay() {
        playCount++;
    }

    void addLike() {
        likes++;
    }

    void removeLike() {
        likes--;
    }

    void addForward() {
        forwardCount++;
    }

    void addCoins(int amount) {
        coins += amount;
    }

    void addComment(int commentId, String content) {
        commentIds.add(commentId);
        commentContents.add(content);
    }

    int[] cleanSpam(String keyword) {
        int removed = 0;
        int maxKeywordCount = 0;
        List<Integer> newIds = new ArrayList<>();
        List<String> newContents = new ArrayList<>();

        for (int i = 0; i < commentIds.size(); i++) {
            String content = commentContents.get(i);
            if (content.contains(keyword)) {
                removed++;
                maxKeywordCount = Math.max(maxKeywordCount, countKeyword(content, keyword));
            } else {
                newIds.add(commentIds.get(i));
                newContents.add(content);
            }
        }

        commentIds.clear();
        commentContents.clear();
        commentIds.addAll(newIds);
        commentContents.addAll(newContents);

        return new int[]{removed, maxKeywordCount};
    }

    static int countKeyword(String content, String keyword) {
        if (keyword.isEmpty()) {
            return content.length() + 1;
        }
        int count = 0;
        int len = keyword.length();
        for (int j = 0; j + len <= content.length(); j++) {
            if (content.substring(j, j + len).equals(keyword)) {
                count++;
            }
        }
        return count;
    }

    public boolean strictEquals(VideoInterface other) {
        if (!(other instanceof Video)) {
            return false;
        }
        Video o = (Video) other;
        if (id != o.id || uploaderId != o.uploaderId || playCount != o.playCount
                || likes != o.likes || forwardCount != o.forwardCount || coins != o.coins) {
            return false;
        }
        if (!type.equals(o.type)) {
            return false;
        }
        return commentIds.equals(o.commentIds) && commentContents.equals(o.commentContents);
    }
}

import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class User implements UserInterface {

    private static final String[] TYPES = {
        "tech", "music", "sport", "game",
        "food", "travel", "comedy"
    };

    private final int id;
    private final String name;
    private final int age;
    private int coins;
    private final List<UserInterface> following;
    private final List<UserInterface> followers;
    private final Set<Integer> followingIds;
    private final Set<Integer> followerIds;
    private final LinkedList<Integer> receivedVideos;
    private final List<VideoInterface> watchedVideos;
    private final Set<Integer> watchedVideoIds;
    private final List<VideoInterface> likedVideos;
    private final Set<Integer> likedVideoIds;
    private final List<Integer> medals;
    private final Set<Integer> medalSet;
    private final LinkedHashMap<Integer, Integer> contributionByUserId;
    private final int[] typeCounts;
    private final List<VideoInterface> videos;

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.coins = 0;
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.followingIds = new HashSet<>();
        this.followerIds = new HashSet<>();
        this.receivedVideos = new LinkedList<>();
        this.watchedVideos = new ArrayList<>();
        this.watchedVideoIds = new HashSet<>();
        this.likedVideos = new ArrayList<>();
        this.likedVideoIds = new HashSet<>();
        this.medals = new ArrayList<>();
        this.medalSet = new HashSet<>();
        this.contributionByUserId = new LinkedHashMap<>();
        this.typeCounts = new int[TYPES.length];
        this.videos = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public int getCoins() {
        return coins;
    }

    @Override
    public boolean isFollowing(UserInterface user) {
        return followingIds.contains(user.getId());
    }

    @Override
    public boolean containsFollower(UserInterface user) {
        return followerIds.contains(user.getId());
    }

    @Override
    public boolean hasReceivedVideo(VideoInterface video) {
        return receivedVideos.contains(video.getId());
    }

    @Override
    public boolean hasWatchedVideo(VideoInterface video) {
        return watchedVideoIds.contains(video.getId());
    }

    @Override
    public boolean hasLikedVideo(VideoInterface video) {
        return likedVideoIds.contains(video.getId());
    }

    @Override
    public boolean hasMedal(int uploaderId) {
        return medalSet.contains(uploaderId);
    }

    @Override
    public int getInterest(String type, int totalVideos) {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(type)) {
                return typeCounts[i]
                        * (totalVideos - watchedVideos.size() + 1);
            }
        }
        return 0;
    }

    @Override
    public int getInfluence(String type) {
        int sum = 0;
        for (VideoInterface v : videos) {
            if (v.getType().equals(type)) {
                sum += v.getHeat();
            }
        }
        return sum;
    }

    @Override
    public List<Integer> getProfile(int totalVideos) {
        List<Integer> profile = new ArrayList<>(TYPES.length);
        for (String type : TYPES) {
            profile.add(getInterest(type, totalVideos));
        }
        return profile;
    }

    @Override
    public long computeUpScore(UserInterface up, int totalVideos) {
        long score = 0;
        for (int i = 0; i < TYPES.length; i++) {
            score += (long) getInterest(TYPES[i], totalVideos)
                    * up.getInfluence(TYPES[i]);
        }
        return score;
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos() {
        int take = Math.min(5, receivedVideos.size());
        List<Integer> list = new ArrayList<>(take);
        int i = 0;
        for (int vid : receivedVideos) {
            if (i >= take) {
                break;
            }
            list.add(vid);
            i++;
        }
        return list;
    }

    @Override
    public double[] queryAgeRatio() {
        double[] ratios = new double[4];
        if (followers.isEmpty()) {
            return ratios;
        }
        int n = followers.size();
        int bucket0 = 0;
        int bucket1 = 0;
        int bucket2 = 0;
        int bucket3 = 0;
        for (UserInterface u : followers) {
            int a = u.getAge();
            if (a <= 16) {
                bucket0++;
            } else if (a <= 30) {
                bucket1++;
            } else if (a <= 45) {
                bucket2++;
            } else {
                bucket3++;
            }
        }
        ratios[0] = 1.0 * bucket0 / n;
        ratios[1] = 1.0 * bucket1 / n;
        ratios[2] = 1.0 * bucket2 / n;
        ratios[3] = 1.0 * bucket3 / n;
        return ratios;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof UserInterface) {
            return ((UserInterface) obj).getId() == id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    List<UserInterface> outNeighbors() {
        return new ArrayList<>(following);
    }

    List<VideoInterface> getWatchedVideos() {
        return watchedVideos;
    }

    void addFollowing(UserInterface user) {
        if (!followingIds.contains(user.getId())) {
            followingIds.add(user.getId());
            following.add(user);
        }
    }

    void addFollower(UserInterface user) {
        if (!followerIds.contains(user.getId())) {
            followerIds.add(user.getId());
            followers.add(user);
        }
    }

    void removeFollowing(UserInterface user) {
        followingIds.remove(user.getId());
        following.removeIf(u -> u.getId() == user.getId());
    }

    void removeFollower(UserInterface user) {
        followerIds.remove(user.getId());
        followers.removeIf(u -> u.getId() == user.getId());
    }

    void prependReceivedVideo(int videoId) {
        receivedVideos.addFirst(videoId);
    }

    void removeReceivedVideo(int videoId) {
        receivedVideos.removeIf(v -> v == videoId);
    }

    void watchVideo(VideoInterface video) {
        removeReceivedVideo(video.getId());
        if (!watchedVideoIds.contains(video.getId())) {
            watchedVideoIds.add(video.getId());
            watchedVideos.add(video);
        }
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(video.getType())) {
                typeCounts[i]++;
                break;
            }
        }
    }

    void addLike(VideoInterface video) {
        if (!likedVideoIds.contains(video.getId())) {
            likedVideoIds.add(video.getId());
            likedVideos.add(video);
        }
    }

    void removeLike(VideoInterface video) {
        likedVideoIds.remove(video.getId());
        likedVideos.removeIf(v -> v.getId() == video.getId());
    }

    void addCoins(int amount) {
        coins += amount;
    }

    void spendCoins(int amount) {
        coins -= amount;
    }

    boolean hasContributor(int contributorId) {
        return contributionByUserId.containsKey(contributorId);
    }

    void addFirstContribution(int contributorId, int amount) {
        contributionByUserId.put(contributorId, amount);
    }

    void addMoreContribution(int contributorId, int amount) {
        contributionByUserId.merge(contributorId, amount,
                Integer::sum);
    }

    int queryBestContributorId() {
        int maxCoins = -1;
        int bestId = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e
                : contributionByUserId.entrySet()) {
            int uid = e.getKey();
            int val = e.getValue();
            if (val > maxCoins || (val == maxCoins && uid < bestId)) {
                maxCoins = val;
                bestId = uid;
            }
        }
        return bestId;
    }

    boolean hasContributors() {
        return !contributionByUserId.isEmpty();
    }

    void addMedal(int uploaderId) {
        if (!medalSet.contains(uploaderId)) {
            medalSet.add(uploaderId);
            medals.add(uploaderId);
        }
    }

    void addVideo(VideoInterface video) {
        videos.add(video);
    }

    public boolean strictEquals(UserInterface other) {
        if (!(other instanceof User)) {
            return false;
        }
        User o = (User) other;
        if (id != o.id || age != o.age || coins != o.coins
                || !name.equals(o.name)) {
            return false;
        }
        if (following.size() != o.following.size()) {
            return false;
        }
        if (followers.size() != o.followers.size()) {
            return false;
        }
        if (receivedVideos.size() != o.receivedVideos.size()) {
            return false;
        }
        if (watchedVideos.size() != o.watchedVideos.size()) {
            return false;
        }
        if (likedVideos.size() != o.likedVideos.size()) {
            return false;
        }
        if (medals.size() != o.medals.size()) {
            return false;
        }
        if (contributionByUserId.size() != o.contributionByUserId.size()) {
            return false;
        }
        if (videos.size() != o.videos.size()) {
            return false;
        }
        for (int i = 0; i < following.size(); i++) {
            if (!following.get(i).equals(o.following.get(i))) {
                return false;
            }
        }
        for (int i = 0; i < followers.size(); i++) {
            if (!followers.get(i).equals(o.followers.get(i))) {
                return false;
            }
        }
        if (!receivedVideos.equals(o.receivedVideos)) {
            return false;
        }
        for (int i = 0; i < watchedVideos.size(); i++) {
            if (!watchedVideos.get(i).equals(o.watchedVideos.get(i))) {
                return false;
            }
        }
        for (int i = 0; i < likedVideos.size(); i++) {
            if (!likedVideos.get(i).equals(o.likedVideos.get(i))) {
                return false;
            }
        }
        if (!medals.equals(o.medals)) {
            return false;
        }
        if (!Arrays.equals(typeCounts, o.typeCounts)) {
            return false;
        }
        for (int i = 0; i < videos.size(); i++) {
            if (!videos.get(i).equals(o.videos.get(i))) {
                return false;
            }
        }
        return contributionByUserId.equals(o.contributionByUserId);
    }
}

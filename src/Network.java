import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.ColdStartVideoException;
import com.oocourse.spec3.exceptions.DuplicateMedalException;
import com.oocourse.spec3.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec3.exceptions.EqualCommentIdException;
import com.oocourse.spec3.exceptions.EqualUserIdException;
import com.oocourse.spec3.exceptions.EqualVideoIdException;
import com.oocourse.spec3.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec3.exceptions.InsufficientCoinsException;
import com.oocourse.spec3.exceptions.InvalidCoinsException;
import com.oocourse.spec3.exceptions.InvalidAgeException;
import com.oocourse.spec3.exceptions.InvalidCommentException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.InvalidTypeException;
import com.oocourse.spec3.exceptions.NoContributorsException;
import com.oocourse.spec3.exceptions.NoUserException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.SelfSubscriptionException;
import com.oocourse.spec3.exceptions.UncessException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoIdNotFoundException;
import com.oocourse.spec3.exceptions.VideoUnwatchedException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Network implements NetworkInterface {

    private final List<UserInterface> users;
    private final List<VideoInterface> videos;
    private final Map<Integer, UserInterface> userMap;
    private final Map<Integer, VideoInterface> videoMap;
    private int mutualFollowingSum;

    public Network() {
        users = new ArrayList<>();
        videos = new ArrayList<>();
        userMap = new HashMap<>();
        videoMap = new HashMap<>();
        mutualFollowingSum = 0;
    }

    public UserInterface[] getUsers() {
        return users.toArray(new UserInterface[0]);
    }

    @Override
    public boolean containsUser(int id) {
        return userMap.containsKey(id);
    }

    @Override
    public UserInterface getUser(int id) {
        return userMap.get(id);
    }

    @Override
    public boolean containsVideo(int id) {
        return videoMap.containsKey(id);
    }

    @Override
    public VideoInterface getVideo(int id) {
        return videoMap.get(id);
    }

    @Override
    public void addUser(int id, String name, int age)
            throws EqualUserIdException, InvalidAgeException {
        if (containsUser(id)) {
            throw new EqualUserIdException(id);
        }
        if (age < 0 || age > 110) {
            throw new InvalidAgeException(age);
        }
        User user = new User(id, name, age);
        users.add(user);
        userMap.put(id, user);
        System.out.println("add_user succeeded");
    }

    @Override
    public boolean isValidType(String type) {
        return "tech".equals(type) || "music".equals(type)
                || "sport".equals(type) || "game".equals(type)
                || "food".equals(type) || "travel".equals(type)
                || "comedy".equals(type);
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId, String type)
            throws UserIdNotFoundException, EqualVideoIdException,
            InvalidTypeException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        UserInterface uploader = getUser(uploaderId);
        Video video = new Video(videoId, uploaderId, type);
        videos.add(video);
        videoMap.put(videoId, video);
        ((User) uploader).addVideo(video);
        for (UserInterface u : users) {
            if (uploader.containsFollower(u)) {
                ((User) u).prependReceivedVideo(videoId);
            }
        }
        System.out.println("upload_video succeeded");
    }

    @Override
    public void followUser(int id1, int id2)
            throws UserIdNotFoundException, SelfSubscriptionException,
            DuplicateSubscriptionException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            throw new SelfSubscriptionException(id1);
        }
        UserInterface u1 = getUser(id1);
        UserInterface u2 = getUser(id2);
        if (u1.isFollowing(u2)) {
            throw new DuplicateSubscriptionException(id1, id2);
        }
        if (u2.isFollowing(u1)) {
            mutualFollowingSum++;
        }
        ((User) u1).addFollowing(u2);
        ((User) u2).addFollower(u1);
        System.out.println("follow_user succeeded");
    }

    @Override
    public void unfollowUser(int id1, int id2)
            throws UserIdNotFoundException, FollowLinkNotFoundException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        UserInterface u1 = getUser(id1);
        UserInterface u2 = getUser(id2);
        if (!u1.isFollowing(u2)) {
            throw new FollowLinkNotFoundException(id1, id2);
        }
        if (u2.isFollowing(u1)) {
            mutualFollowingSum--;
        }
        ((User) u1).removeFollowing(u2);
        ((User) u2).removeFollower(u1);
        System.out.println("unfollow_user succeeded");
    }

    @Override
    public void watchVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        VideoInterface video = getVideo(videoId);
        ((User) getUser(userId)).watchVideo(video);
        ((Video) video).addPlay();
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId)
            throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        return getUser(userId).queryReceivedUnwatchedVideos();
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId)
            throws UserIdNotFoundException {
        if (!containsUser(upId)) {
            throw new UserIdNotFoundException(upId);
        }
        return getUser(upId).queryAgeRatio();
    }

    @Override
    public int queryMutualFollowingSum() {
        return mutualFollowingSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws UserIdNotFoundException, UncessException {
        if (!containsUser(id1)) {
            throw new UserIdNotFoundException(id1);
        }
        if (!containsUser(id2)) {
            throw new UserIdNotFoundException(id2);
        }
        if (id1 == id2) {
            return 0;
        }
        UserInterface from = getUser(id1);
        Queue<UserInterface> queue = new LinkedList<>();
        Map<Integer, Integer> depth = new HashMap<>();
        queue.add(from);
        depth.put(from.getId(), 0);
        while (!queue.isEmpty()) {
            UserInterface cur = queue.remove();
            int d = depth.get(cur.getId());
            for (UserInterface next : ((User) cur).outNeighbors()) {
                if (!depth.containsKey(next.getId())) {
                    int nextD = d + 1;
                    if (next.getId() == id2) {
                        return nextD;
                    }
                    depth.put(next.getId(), nextD);
                    queue.add(next);
                }
            }
        }
        throw new UncessException(id1, id2);
    }

    @Override
    public void addUserCoins(int userId, int coins)
            throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        ((User) getUser(userId)).addCoins(coins);
        System.out.println("add_user_coins succeeded");
    }

    @Override
    public void likeVideo(int userId, int videoId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            VideoUnwatchedException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        VideoInterface video = getVideo(videoId);
        if (userId == video.getUploaderId()) {
            throw new EqualUserIdException(userId);
        }
        if (!user.hasWatchedVideo(video)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        Video v = (Video) video;
        if (user.hasLikedVideo(video)) {
            user.removeLike(video);
            v.removeLike();
            System.out.println("unlike_video succeeded");
        } else {
            user.addLike(video);
            v.addLike();
            System.out.println("like_video succeeded");
        }
    }

    @Override
    public void coinVideo(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            InsufficientCoinsException, VideoUnwatchedException,
            InvalidCoinsException, EqualUserIdException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        VideoInterface video = getVideo(videoId);
        int uploaderId = video.getUploaderId();
        if (userId == uploaderId) {
            throw new EqualUserIdException(userId);
        }
        if (!user.hasWatchedVideo(video)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        if (amount != 1 && amount != 2) {
            throw new InvalidCoinsException(amount);
        }
        if (user.getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        user.spendCoins(amount);
        Video v = (Video) video;
        v.addCoins(amount);
        User uploader = (User) getUser(uploaderId);
        uploader.addCoins(amount);
        if (uploader.hasContributor(userId)) {
            uploader.addMoreContribution(userId, amount);
        } else {
            uploader.addFirstContribution(userId, amount);
        }
        System.out.println("coin_video succeeded");
    }

    @Override
    public int queryBestContributor(int id)
            throws UserIdNotFoundException, NoContributorsException {
        if (!containsUser(id)) {
            throw new UserIdNotFoundException(id);
        }
        User user = (User) getUser(id);
        if (!user.hasContributors()) {
            throw new NoContributorsException(id);
        }
        return user.queryBestContributorId();
    }

    @Override
    public void forwardVideo(int userId, int videoId, int followerId)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            FollowLinkNotFoundException, VideoUnwatchedException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsUser(followerId)) {
            throw new UserIdNotFoundException(followerId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        VideoInterface video = getVideo(videoId);
        if (!user.hasWatchedVideo(video)) {
            throw new VideoUnwatchedException(userId, videoId);
        }
        UserInterface follower = getUser(followerId);
        if (!user.containsFollower(follower)) {
            throw new FollowLinkNotFoundException(followerId, userId);
        }
        ((User) follower).prependReceivedVideo(videoId);
        ((Video) video).addForward();
        System.out.println("forward_video succeeded");
    }

    @Override
    public void sendComment(int userId, int videoId, int commentId,
                            String comment)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            EqualCommentIdException, InvalidCommentException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        Video v = (Video) getVideo(videoId);
        if (v.containsComment(commentId)) {
            throw new EqualCommentIdException(commentId);
        }
        if (comment == null || comment.isEmpty()) {
            throw new InvalidCommentException();
        }
        v.addComment(commentId, comment);
        System.out.println("send_comment succeeded");
    }

    @Override
    public int[] cleanSpamComments(int videoId, String keyword)
            throws VideoIdNotFoundException {
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        return ((Video) getVideo(videoId)).cleanSpam(keyword);
    }

    @Override
    public VideoInterface queryMostPopularVideo(String type)
            throws InvalidTypeException {
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        VideoInterface best = null;
        int bestHeat = -1;
        for (VideoInterface v : videos) {
            if (!v.getType().equals(type)) {
                continue;
            }
            int heat = v.getHeat();
            if (best == null || heat > bestHeat
                    || (heat == bestHeat && v.getId() < best.getId())) {
                best = v;
                bestHeat = heat;
            }
        }
        return best;
    }

    @Override
    public void purchaseMedal(int userId, int videoId, int amount)
            throws UserIdNotFoundException, VideoIdNotFoundException,
            EqualUserIdException, InsufficientCoinsException,
            DuplicateMedalException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (!containsVideo(videoId)) {
            throw new VideoIdNotFoundException(videoId);
        }
        User user = (User) getUser(userId);
        int uploaderId = getVideo(videoId).getUploaderId();
        if (userId == uploaderId) {
            throw new EqualUserIdException(userId);
        }
        if (user.getCoins() < amount) {
            throw new InsufficientCoinsException(userId);
        }
        if (user.hasMedal(uploaderId)) {
            throw new DuplicateMedalException(userId, uploaderId);
        }
        user.spendCoins(amount);
        ((User) getUser(uploaderId)).addCoins(amount);
        user.addMedal(uploaderId);
        System.out.println("purchase_medal succeeded");
    }

    @Override
    public int queryLongestDecSeq() {
        if (users.isEmpty()) {
            return 0;
        }
        List<UserInterface> sorted = new ArrayList<>(users);
        sorted.sort((a, b) -> Integer.compare(a.getAge(), b.getAge()));
        Map<Integer, Integer> dp = new HashMap<>();
        int ans = 0;
        for (UserInterface u : sorted) {
            int len = 1;
            for (UserInterface v : ((User) u).outNeighbors()) {
                if (u.getAge() > v.getAge()) {
                    len = Math.max(len,
                            1 + dp.getOrDefault(v.getId(), 1));
                }
            }
            dp.put(u.getId(), len);
            ans = Math.max(ans, len);
        }
        return ans;
    }

    @Override
    public int[] queryGlobalBestContributor() throws NoUserException {
        if (users.isEmpty()) {
            throw new NoUserException();
        }
        Map<Integer, Integer> countMap = new HashMap<>();
        for (UserInterface u : users) {
            if (((User) u).hasContributors()) {
                int bestId = ((User) u).queryBestContributorId();
                countMap.merge(bestId, 1, Integer::sum);
            }
        }
        if (countMap.isEmpty()) {
            return new int[]{0, 0};
        }
        int maxCount = Collections.max(countMap.values());
        int minId = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> e : countMap.entrySet()) {
            if (e.getValue() == maxCount && e.getKey() < minId) {
                minId = e.getKey();
            }
        }
        return new int[]{minId, maxCount};
    }

    @Override
    public int recommendVideo(int userId)
            throws UserIdNotFoundException, NoVideoUploadedException,
            ColdStartVideoException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (videos.isEmpty()) {
            throw new NoVideoUploadedException();
        }
        UserInterface user = getUser(userId);
        if (((User) user).getWatchedVideos().isEmpty()) {
            throw new ColdStartVideoException(userId);
        }
        int bestId = -1;
        long bestScore = -1;
        for (VideoInterface v : videos) {
            long score = computeVideoScore(user, v);
            if (score > bestScore
                    || (score == bestScore && v.getId() < bestId)) {
                bestScore = score;
                bestId = v.getId();
            }
        }
        return bestId;
    }

    @Override
    public long computeVideoScore(UserInterface user,
                                  VideoInterface video) {
        return (long) video.getHeat()
                * user.getInterest(video.getType(), videos.size());
    }

    @Override
    public int recommendNthUp(int userId, int rank)
            throws UserIdNotFoundException, InvalidRankException,
            NoVideoUploadedException, ColdStartUserException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        if (rank <= 0) {
            throw new InvalidRankException(rank);
        }
        if (videos.isEmpty()) {
            throw new NoVideoUploadedException();
        }
        UserInterface user = getUser(userId);
        List<UserInterface> candidates = new ArrayList<>();
        for (UserInterface u : users) {
            if (u.getId() != userId && !user.isFollowing(u)) {
                candidates.add(u);
            }
        }
        if (candidates.size() < rank) {
            throw new ColdStartUserException(userId);
        }
        candidates.sort((a, b) -> {
            long scoreA = user.computeUpScore(a, videos.size());
            long scoreB = user.computeUpScore(b, videos.size());
            if (scoreB != scoreA) {
                return Long.compare(scoreB, scoreA);
            }
            return Integer.compare(a.getId(), b.getId());
        });
        return candidates.get(rank - 1).getId();
    }

    @Override
    public int queryMostInfluentialUp(String type)
            throws InvalidTypeException, NoUserException {
        if (!isValidType(type)) {
            throw new InvalidTypeException(type);
        }
        if (users.isEmpty()) {
            throw new NoUserException();
        }
        int bestId = -1;
        int bestInfluence = -1;
        for (UserInterface u : users) {
            int inf = u.getInfluence(type);
            if (inf > bestInfluence
                    || (inf == bestInfluence && u.getId() < bestId)) {
                bestInfluence = inf;
                bestId = u.getId();
            }
        }
        return bestId;
    }

    @Override
    public List<Integer> queryUserProfile(int userId)
            throws UserIdNotFoundException, ColdStartVideoException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        UserInterface user = getUser(userId);
        if (((User) user).getWatchedVideos().isEmpty()) {
            throw new ColdStartVideoException(userId);
        }
        return user.getProfile(videos.size());
    }
}

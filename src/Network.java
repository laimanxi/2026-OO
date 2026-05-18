import com.oocourse.spec1.exceptions.DuplicateSubscriptionException;
import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.EqualVideoIdException;
import com.oocourse.spec1.exceptions.FollowLinkNotFoundException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.exceptions.SelfSubscriptionException;
import com.oocourse.spec1.exceptions.UncessException;
import com.oocourse.spec1.exceptions.UserIdNotFoundException;
import com.oocourse.spec1.exceptions.VideoIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.UserInterface;
import com.oocourse.spec1.main.VideoInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Network implements NetworkInterface {

    private final List<UserInterface> users;
    private final List<VideoInterface> videos;
    private int mutualFollowingSum;

    public Network() {
        users = new ArrayList<>();
        videos = new ArrayList<>();
        mutualFollowingSum = 0;
    }

    public UserInterface[] getUsers() {
        return users.toArray(new UserInterface[0]);
    }

    @Override
    public boolean containsUser(int id) {
        return getUser(id) != null;
    }

    @Override
    public UserInterface getUser(int id) {
        for (UserInterface u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    @Override
    public boolean containsVideo(int id) {
        return getVideo(id) != null;
    }

    @Override
    public VideoInterface getVideo(int id) {
        for (VideoInterface v : videos) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
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
        users.add(new User(id, name, age));
        System.out.println("add_user succeeded");
    }

    @Override
    public void uploadVideo(int uploaderId, int videoId)
            throws UserIdNotFoundException, EqualVideoIdException {
        if (!containsUser(uploaderId)) {
            throw new UserIdNotFoundException(uploaderId);
        }
        if (containsVideo(videoId)) {
            throw new EqualVideoIdException(videoId);
        }
        UserInterface uploader = getUser(uploaderId);
        videos.add(new Video(videoId, uploaderId));
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
        ((User) getUser(userId)).removeReceivedVideo(videoId);
        System.out.println("watch_video succeeded");
    }

    @Override
    public List<Integer> queryReceivedUnwatchedVideos(int userId) throws UserIdNotFoundException {
        if (!containsUser(userId)) {
            throw new UserIdNotFoundException(userId);
        }
        return getUser(userId).queryReceivedUnwatchedVideos();
    }

    @Override
    public double[] queryUpFollowersAgeRatio(int upId) throws UserIdNotFoundException {
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
    public int queryShortestPath(int id1, int id2) throws UserIdNotFoundException, UncessException {
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
}

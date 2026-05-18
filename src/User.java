import com.oocourse.spec1.main.UserInterface;
import com.oocourse.spec1.main.VideoInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements UserInterface {

    private final int id;
    private final String name;
    private final int age;
    private final List<UserInterface> following;
    private final List<UserInterface> followers;
    private final List<Integer> receivedVideos;

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.receivedVideos = new ArrayList<>();
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
    public boolean isFollowing(UserInterface user) {
        for (UserInterface u : following) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsFollower(UserInterface user) {
        for (UserInterface u : followers) {
            if (u.getId() == user.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasReceivedVideo(VideoInterface video) {
        int vid = video.getId();
        for (Integer v : receivedVideos) {
            if (v == vid) {
                return true;
            }
        }
        return false;
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
    public List<Integer> queryReceivedUnwatchedVideos() {
        int take = Math.min(5, receivedVideos.size());
        List<Integer> list = new ArrayList<>(take);
        for (int i = 0; i < take; i++) {
            list.add(receivedVideos.get(i));
        }
        return list;
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
        return following;
    }

    void addFollowing(UserInterface user) {
        following.add(user);
    }

    void addFollower(UserInterface user) {
        followers.add(user);
    }

    void removeFollowing(UserInterface user) {
        following.removeIf(u -> u.getId() == user.getId());
    }

    void removeFollower(UserInterface user) {
        followers.removeIf(u -> u.getId() == user.getId());
    }

    void prependReceivedVideo(int videoId) {
        receivedVideos.add(0, videoId);
    }

    void removeReceivedVideo(int videoId) {
        receivedVideos.removeIf(v -> v == videoId);
    }

    /** 供 JUnit 比对状态：非容器字段用 == / equals；容器内元素按位 equals。 */
    public boolean strictEquals(UserInterface other) {
        if (!(other instanceof User)) {
            return false;
        }
        User o = (User) other;
        if (id != o.id || age != o.age || !name.equals(o.name)) {
            return false;
        }
        if (following.size() != o.following.size()
                || followers.size() != o.followers.size()
                || receivedVideos.size() != o.receivedVideos.size()) {
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
        for (int i = 0; i < receivedVideos.size(); i++) {
            if (!Objects.equals(receivedVideos.get(i), o.receivedVideos.get(i))) {
                return false;
            }
        }
        return true;
    }
}

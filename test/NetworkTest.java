import com.oocourse.spec3.exceptions.ColdStartUserException;
import com.oocourse.spec3.exceptions.InvalidRankException;
import com.oocourse.spec3.exceptions.NoVideoUploadedException;
import com.oocourse.spec3.exceptions.UserIdNotFoundException;
import com.oocourse.spec3.main.UserInterface;
import com.oocourse.spec3.main.VideoInterface;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    private Network network;

    @Before
    public void setUp() {
        network = new Network();
    }

    private void setupBasicNetwork() throws Exception {
        network.addUser(1, "Alice", 20);
        network.addUser(2, "Bob", 22);
        network.addUser(3, "Charlie", 25);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(3, 102, "tech");
        network.watchVideo(1, 101);
        network.likeVideo(1, 101);
        network.watchVideo(1, 102);
    }

    private void setupNetworkWithFollow() throws Exception {
        setupBasicNetwork();
        network.followUser(1, 2);
    }

private void assertUsersUnchanged(Network before, Network after) {
        UserInterface[] bu = before.getUsers();
        UserInterface[] au = after.getUsers();
        assertEquals(bu.length, au.length);
        for (int i = 0; i < bu.length; i++) {
            assertTrue(((User) bu[i]).strictEquals(au[i]));
        }
    }

    @Test
    public void exceptionUserIdNotFound() throws Exception {
        try {
            network.recommendNthUp(999, 1);
            assertTrue(false);
        } catch (UserIdNotFoundException e) {
            assertTrue(true);
        }
    }

    @Test
    public void exceptionInvalidRankZero() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        try {
            network.recommendNthUp(1, 0);
            assertTrue(false);
        } catch (InvalidRankException e) {
            assertTrue(true);
        }
    }

    @Test
    public void exceptionInvalidRankNegative() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        try {
            network.recommendNthUp(1, -1);
            assertTrue(false);
        } catch (InvalidRankException e) {
            assertTrue(true);
        }
    }

    @Test
    public void exceptionNoVideoUploaded() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        try {
            network.recommendNthUp(1, 1);
            assertTrue(false);
        } catch (NoVideoUploadedException e) {
            assertTrue(true);
        }
    }

    @Test
    public void exceptionColdStartUserAllFollowed() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.uploadVideo(2, 101, "tech");
        network.followUser(1, 2);
        network.followUser(1, 3);
        try {
            network.recommendNthUp(1, 1);
            assertTrue(false);
        } catch (ColdStartUserException e) {
            assertTrue(true);
        }
    }

    @Test
    public void exceptionColdStartUserInsufficient() throws Exception {
        setupBasicNetwork();
        try {
            network.recommendNthUp(1, 3);
            assertTrue(false);
        } catch (ColdStartUserException e) {
            assertTrue(true);
        }
    }

    @Test
    public void normalRankOneBasic() throws Exception {
        setupBasicNetwork();
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void normalRankTwoBasic() throws Exception {
        setupBasicNetwork();
        int result = network.recommendNthUp(1, 2);
        assertEquals(3, result);
    }

    @Test
    public void normalTieBreakSmallerId() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(3, 102, "tech");
        network.watchVideo(1, 101);
        network.watchVideo(1, 102);
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void normalExcludeSelf() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        network.addUser(2, "B", 22);
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void normalExcludeFollowed() throws Exception {
        setupNetworkWithFollow();
        int result = network.recommendNthUp(1, 1);
        assertEquals(3, result);
    }

    @Test
    public void pureUsersUnchanged() throws Exception {
        setupBasicNetwork();
        Network snapshot = new Network();
        snapshot.addUser(1, "Alice", 20);
        snapshot.addUser(2, "Bob", 22);
        snapshot.addUser(3, "Charlie", 25);
        snapshot.uploadVideo(2, 101, "tech");
        snapshot.uploadVideo(3, 102, "tech");
        snapshot.watchVideo(1, 101);
        snapshot.likeVideo(1, 101);
        snapshot.watchVideo(1, 102);
        network.recommendNthUp(1, 1);
        assertUsersUnchanged(snapshot, network);
    }

    @Test
    public void pureVideosUnchanged() throws Exception {
        setupBasicNetwork();
        network.recommendNthUp(1, 1);
        VideoInterface v101 = network.getVideo(101);
        assertEquals(1, v101.getPlayCount());
        assertEquals(1, v101.getLikes());
        assertEquals("tech", v101.getType());
        VideoInterface v102 = network.getVideo(102);
        assertEquals(1, v102.getPlayCount());
        assertEquals(0, v102.getLikes());
    }

    @Test
    public void pureFollowRelationsUnchanged() throws Exception {
        setupBasicNetwork();
        network.followUser(1, 2);
        network.recommendNthUp(1, 1);
        assertTrue(network.getUser(1).isFollowing(network.getUser(2)));
        assertTrue(network.getUser(2).containsFollower(network.getUser(1)));
        assertFalse(network.getUser(1).isFollowing(network.getUser(3)));
    }

    @Test
    public void pureMultipleCallsIdempotent() throws Exception {
        setupBasicNetwork();
        int r1 = network.recommendNthUp(1, 1);
        int r2 = network.recommendNthUp(1, 1);
        assertEquals(r1, r2);
    }

    @Test
    public void boundaryExactlyRankCandidates() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.uploadVideo(2, 101, "tech");
        network.followUser(1, 2);
        int result = network.recommendNthUp(1, 1);
        assertEquals(3, result);
    }

    @Test
    public void boundarySingleCandidate() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void boundaryLargeIdDifference() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(100000, "B", 22);
        network.uploadVideo(100000, 101, "tech");
        network.watchVideo(1, 101);
        int result = network.recommendNthUp(1, 1);
        assertEquals(100000, result);
    }

    @Test
    public void complexZeroInfluence() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.uploadVideo(2, 101, "tech");
        network.watchVideo(1, 101);
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void complexMixedTypes() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(2, 102, "music");
        network.uploadVideo(3, 103, "tech");
        network.watchVideo(1, 101);
        network.watchVideo(1, 102);
        network.watchVideo(1, 103);
        int result = network.recommendNthUp(1, 1);
        assertEquals(2, result);
    }

    @Test
    public void noSideEffectOnOtherVideoFields() throws Exception {
        setupBasicNetwork();
        VideoInterface before = network.getVideo(101);
        int play = before.getPlayCount();
        int likes = before.getLikes();
        int forwards = before.getForwardCount();
        int coins = before.getCoins();
        String type = before.getType();
        network.recommendNthUp(1, 1);
        VideoInterface after = network.getVideo(101);
        assertEquals(play, after.getPlayCount());
        assertEquals(likes, after.getLikes());
        assertEquals(forwards, after.getForwardCount());
        assertEquals(coins, after.getCoins());
        assertEquals(type, after.getType());
        assertEquals(101, after.getId());
        assertEquals(2, after.getUploaderId());
    }

    @Test
    public void noSideEffectOnOtherUsers() throws Exception {
        setupBasicNetwork();
        network.followUser(1, 2);
        Network snapshot = new Network();
        snapshot.addUser(1, "Alice", 20);
        snapshot.addUser(2, "Bob", 22);
        snapshot.addUser(3, "Charlie", 25);
        snapshot.uploadVideo(2, 101, "tech");
        snapshot.uploadVideo(3, 102, "tech");
        snapshot.watchVideo(1, 101);
        snapshot.likeVideo(1, 101);
        snapshot.watchVideo(1, 102);
        snapshot.followUser(1, 2);
        network.recommendNthUp(1, 1);
        assertUsersUnchanged(snapshot, network);
    }

    @Test
    public void recommendUpWithNoFollowing() throws Exception {
        setupBasicNetwork();
        int r1 = network.recommendNthUp(1, 1);
        int r2 = network.recommendNthUp(1, 2);
        assertEquals(2, r1);
        assertEquals(3, r2);
    }

    @Test
    public void remainingCandidatesOrder() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.addUser(3, "C", 25);
        network.addUser(4, "D", 30);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(3, 102, "tech");
        network.uploadVideo(4, 103, "tech");
        network.watchVideo(1, 101);
        network.watchVideo(1, 102);
        network.watchVideo(1, 103);
        network.likeVideo(1, 101);
        network.addUserCoins(1, 10);
        network.coinVideo(1, 101, 1);
        assertEquals(2, network.recommendNthUp(1, 1));
        assertEquals(3, network.recommendNthUp(1, 2));
        assertEquals(4, network.recommendNthUp(1, 3));
    }

    @Test
    public void repeatWatchUpdatesUserProfile() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(2, 102, "music");
        network.watchVideo(1, 101);
        network.watchVideo(1, 101);

        assertEquals(4, (int) network.queryUserProfile(1).get(0));
        assertEquals(0, (int) network.queryUserProfile(1).get(1));
    }

    @Test
    public void repeatWatchAffectsRecommendVideo() throws Exception {
        setupRepeatWatchRecommendationNetwork();

        assertEquals(101, network.recommendVideo(1));
    }

    @Test
    public void repeatWatchAffectsRecommendNthUp() throws Exception {
        setupRepeatWatchRecommendationNetwork();

        assertEquals(2, network.recommendNthUp(1, 1));
    }

    private void setupRepeatWatchRecommendationNetwork() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "TechUp", 22);
        network.addUser(3, "MusicUp", 25);
        network.uploadVideo(2, 101, "tech");
        network.uploadVideo(3, 201, "music");
        network.watchVideo(1, 101);
        network.watchVideo(1, 101);
        network.watchVideo(1, 201);
        network.likeVideo(1, 201);
    }
}

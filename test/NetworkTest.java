import com.oocourse.spec2.exceptions.VideoIdNotFoundException;
import com.oocourse.spec2.main.UserInterface;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    private Network network;

    @Before
    public void setUp() {
        network = new Network();
    }

    private void setupVideoWithComments() throws Exception {
        network.addUser(1, "Alice", 20);
        network.addUser(2, "Bob", 22);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(2, 101);
        network.sendComment(2, 101, 1, "Awesome!");
        network.sendComment(2, 101, 2, "Bad_Spam");
    }

    @Test
    public void noSpam() throws Exception {
        setupVideoWithComments();
        int[] result = network.cleanSpamComments(101, "xyz");
        assertEquals(2, result.length);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(2, ((Video) network.getVideo(101)).getCommentIds().length);
    }

    @Test
    public void sampleFromGuide() throws Exception {
        setupVideoWithComments();
        int[] result = network.cleanSpamComments(101, "Spam");
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
        Video video = (Video) network.getVideo(101);
        int[] ids = video.getCommentIds();
        assertEquals(1, ids.length);
        assertEquals(1, ids[0]);
        assertEquals("Awesome!", video.getCommentContents()[0]);
    }

    @Test
    public void deleteAllSpam() throws Exception {
        setupVideoWithComments();
        int[] result = network.cleanSpamComments(101, "Bad");
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
        assertEquals(1, ((Video) network.getVideo(101)).getCommentIds().length);
    }

    @Test
    public void overlappingKeywordCount() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        network.sendComment(1, 101, 1, "aaaa");
        int[] result = network.cleanSpamComments(101, "aa");
        assertEquals(1, result[0]);
        assertEquals(3, result[1]);
    }

    @Test
    public void keepNonSpamOrder() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        network.sendComment(1, 101, 1, "good1");
        network.sendComment(1, 101, 2, "spam here");
        network.sendComment(1, 101, 3, "good2");
        network.cleanSpamComments(101, "spam");
        Video video = (Video) network.getVideo(101);
        assertArrayEquals(new int[]{1, 3}, video.getCommentIds());
        assertArrayEquals(new String[]{"good1", "good2"}, video.getCommentContents());
    }

    @Test
    public void videoNotFound() throws Exception {
        try {
            network.cleanSpamComments(999, "x");
            assertTrue(false);
        } catch (VideoIdNotFoundException e) {
            assertTrue(true);
        }
    }

    @Test
    public void noSideEffectOnOtherUsers() throws Exception {
        network.addUser(1, "A", 20);
        network.addUser(2, "B", 22);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(2, 101);
        network.sendComment(2, 101, 1, "Spam_text");
        network.followUser(2, 1);

        Network snapshot = new Network();
        snapshot.addUser(1, "A", 20);
        snapshot.addUser(2, "B", 22);
        snapshot.uploadVideo(1, 101, "tech");
        snapshot.watchVideo(2, 101);
        snapshot.sendComment(2, 101, 1, "Spam_text");
        snapshot.followUser(2, 1);

        network.cleanSpamComments(101, "Spam");

        UserInterface[] before = snapshot.getUsers();
        UserInterface[] after = network.getUsers();
        for (int i = 0; i < before.length; i++) {
            assertTrue(((User) before[i]).strictEquals(after[i]));
        }
    }

    @Test
    public void noSideEffectOnOtherVideoFields() throws Exception {
        setupVideoWithComments();
        Video before = (Video) network.getVideo(101);
        int play = before.getPlayCount();
        int likes = before.getLikes();
        int forwards = before.getForwardCount();
        int coins = before.getCoins();
        String type = before.getType();

        network.cleanSpamComments(101, "Spam");

        Video after = (Video) network.getVideo(101);
        assertEquals(play, after.getPlayCount());
        assertEquals(likes, after.getLikes());
        assertEquals(forwards, after.getForwardCount());
        assertEquals(coins, after.getCoins());
        assertEquals(type, after.getType());
        assertEquals(101, after.getId());
        assertEquals(1, after.getUploaderId());
    }

    @Test
    public void otherVideoCommentsUnchanged() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.uploadVideo(1, 102, "music");
        network.watchVideo(1, 101);
        network.watchVideo(1, 102);
        network.sendComment(1, 101, 1, "spam1");
        network.sendComment(1, 102, 1, "keep");

        Video other = (Video) network.getVideo(102);
        int[] idsBefore = other.getCommentIds();
        network.cleanSpamComments(101, "spam");
        assertArrayEquals(idsBefore, other.getCommentIds());
        assertArrayEquals(new String[]{"keep"}, other.getCommentContents());
    }

    @Test
    public void remainingHaveNoKeyword() throws Exception {
        setupVideoWithComments();
        network.cleanSpamComments(101, "Spam");
        for (String content : ((Video) network.getVideo(101)).getCommentContents()) {
            assertFalse(content.contains("Spam"));
        }
    }

    @Test
    public void multipleSpamMaxCount() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        network.sendComment(1, 101, 1, "ababab");
        network.sendComment(1, 101, 2, "ab");
        int[] result = network.cleanSpamComments(101, "ab");
        assertEquals(2, result[0]);
        assertEquals(3, result[1]);
    }

    @Test
    public void emptyKeywordRemovesAll() throws Exception {
        setupVideoWithComments();
        int[] result = network.cleanSpamComments(101, "");
        assertEquals(2, result[0]);
        assertEquals(9, result[1]);
        assertEquals(0, ((Video) network.getVideo(101)).getCommentIds().length);
    }

    @Test
    public void maxCountOverAllMatchingComments() throws Exception {
        network.addUser(1, "A", 20);
        network.uploadVideo(1, 101, "tech");
        network.watchVideo(1, 101);
        network.sendComment(1, 101, 1, "ababab");
        network.sendComment(1, 101, 2, "good");
        int[] result = network.cleanSpamComments(101, "ab");
        assertEquals(1, result[0]);
        assertEquals(3, result[1]);
    }
}

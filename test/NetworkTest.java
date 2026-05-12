import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.main.UserInterface;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    private static void assertGraphUnchanged(Network net, UserInterface[] beforeUsers) {
        UserInterface[] afterUsers = net.getUsers();
        assertEquals(beforeUsers.length, afterUsers.length);
        for (int i = 0; i < beforeUsers.length; i++) {
            assertSame("纯查询不应替换用户对象引用", beforeUsers[i], afterUsers[i]);
            assertTrue(((User) beforeUsers[i]).strictEquals(afterUsers[i]));
        }
    }

    private static Network networkWithThreeUsers() throws EqualUserIdException, InvalidAgeException {
        Network net = new Network();
        net.addUser(1, "a", 20);
        net.addUser(2, "b", 21);
        net.addUser(3, "c", 22);
        return net;
    }

    @Test
    public void mutualSumEmptyGraph() {
        Network net = new Network();
        UserInterface[] snap = net.getUsers();
        assertEquals(0, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumSingleUser() throws EqualUserIdException, InvalidAgeException {
        Network net = new Network();
        net.addUser(1, "x", 18);
        UserInterface[] snap = net.getUsers();
        assertEquals(0, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumOneWayNoPair() throws Exception {
        Network net = networkWithThreeUsers();
        net.followUser(1, 2);
        UserInterface[] snap = net.getUsers();
        assertEquals(0, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumOneMutualPair() throws Exception {
        Network net = networkWithThreeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        UserInterface[] snap = net.getUsers();
        assertEquals(1, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumMatchesGuideSample() throws Exception {
        Network net = networkWithThreeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(2, 3);
        UserInterface[] snap = net.getUsers();
        assertEquals(1, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumTwoDisjointPairs() throws Exception {
        Network net = new Network();
        net.addUser(1, "a", 20);
        net.addUser(2, "b", 21);
        net.addUser(3, "c", 22);
        net.addUser(4, "d", 23);
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(3, 4);
        net.followUser(4, 3);
        UserInterface[] snap = net.getUsers();
        assertEquals(2, net.queryMutualFollowingSum());
        assertGraphUnchanged(net, snap);
    }

    @Test
    public void mutualSumIdempotentAndPure() throws Exception {
        Network net = networkWithThreeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(2, 3);
        net.followUser(3, 2);
        UserInterface[] snap = net.getUsers();
        int once = net.queryMutualFollowingSum();
        int twice = net.queryMutualFollowingSum();
        assertEquals(2, once);
        assertEquals(once, twice);
        assertGraphUnchanged(net, snap);
    }
}

import com.oocourse.spec1.exceptions.EqualUserIdException;
import com.oocourse.spec1.exceptions.InvalidAgeException;
import com.oocourse.spec1.main.UserInterface;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    private static int specMutualFollowingSum(Network net) {
        UserInterface[] users = net.getUsers();
        int n = users.length;
        int sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (users[i].isFollowing(users[j]) && users[j].isFollowing(users[i])) {
                    sum++;
                }
            }
        }
        return sum;
    }

    private static void assertAfterQueryUnchanged(Network net, UserInterface[] before) {
        UserInterface[] after = net.getUsers();
        assertEquals(before.length, after.length);
        for (int i = 0; i < before.length; i++) {
            assertSame(before[i], after[i]);
            assertTrue(((User) before[i]).strictEquals(after[i]));
        }
    }

    private static void checkSumAndPure(Network net) {
        UserInterface[] before = net.getUsers();
        assertEquals(specMutualFollowingSum(net), net.queryMutualFollowingSum());
        assertAfterQueryUnchanged(net, before);
    }

    private static void checkSumTwiceAndPure(Network net) {
        UserInterface[] before = net.getUsers();
        int want = specMutualFollowingSum(net);
        assertEquals(want, net.queryMutualFollowingSum());
        assertEquals(want, net.queryMutualFollowingSum());
        assertAfterQueryUnchanged(net, before);
    }

    private static Network threeUsers() throws EqualUserIdException, InvalidAgeException {
        Network net = new Network();
        net.addUser(1, "a", 20);
        net.addUser(2, "b", 21);
        net.addUser(3, "c", 22);
        return net;
    }

    @Test
    public void empty() {
        checkSumAndPure(new Network());
    }

    @Test
    public void singleUser() throws EqualUserIdException, InvalidAgeException {
        Network net = new Network();
        net.addUser(1, "x", 18);
        checkSumAndPure(net);
    }

    @Test
    public void oneWayEdge() throws Exception {
        Network net = threeUsers();
        net.followUser(1, 2);
        checkSumAndPure(net);
    }

    @Test
    public void oneMutualPair() throws Exception {
        Network net = threeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        checkSumAndPure(net);
    }

    @Test
    public void guideSample() throws Exception {
        Network net = threeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(2, 3);
        checkSumAndPure(net);
    }

    @Test
    public void twoDisjointMutualPairs() throws Exception {
        Network net = new Network();
        net.addUser(1, "a", 20);
        net.addUser(2, "b", 21);
        net.addUser(3, "c", 22);
        net.addUser(4, "d", 23);
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(3, 4);
        net.followUser(4, 3);
        checkSumAndPure(net);
    }

    @Test
    public void triangleComplete() throws Exception {
        Network net = threeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(2, 3);
        net.followUser(3, 2);
        net.followUser(1, 3);
        net.followUser(3, 1);
        checkSumAndPure(net);
    }

    @Test
    public void callTwice() throws Exception {
        Network net = threeUsers();
        net.followUser(1, 2);
        net.followUser(2, 1);
        net.followUser(2, 3);
        net.followUser(3, 2);
        checkSumTwiceAndPure(net);
    }

    @Test
    public void usersNotOrderedById() throws Exception {
        Network net = new Network();
        net.addUser(10, "p", 20);
        net.addUser(5, "q", 21);
        net.addUser(20, "r", 22);
        net.followUser(10, 5);
        net.followUser(5, 10);
        checkSumAndPure(net);
    }
}

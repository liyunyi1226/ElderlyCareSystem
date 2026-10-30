package util;
import dao.CommunityStaffDAO;
import dao.DoctorDAO;
import dao.ElderlyDAO;
import model.CommunityStaff;
import model.Doctor;
import model.Elderly;
import model.User;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
public class CommunityScope {
    public static boolean isWorker(User u) {
        return u != null && "community_worker".equals(u.getRole());
    }
    public static boolean isDoctor(User u) {
        return u != null && "doctor".equals(u.getRole());
    }
    public static Integer workerCommunityId(User u) throws SQLException {
        if (!isWorker(u)) return null;
        CommunityStaff s = CommunityStaffDAO.findByUserId(u.getId());
        return s != null ? s.getCommunityId() : null;
    }
    public static Integer doctorCommunityId(User u) throws SQLException {
        if (!isDoctor(u)) return null;
        Doctor d = DoctorDAO.findByUserId(u.getId());
        return d != null ? d.getCommunityId() : null;
    }
    public static Map<Integer, Integer> elderlyCommunityMap() throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        for (Elderly e : ElderlyDAO.findAll(null)) {
            Integer cid = e.getCommunityId();
            map.put(e.getId(), cid != null ? cid : 0);
        }
        return map;
    }
    public static <T> void keepOwnCommunity(List<T> list, Integer communityId, Function<T, Integer> elderlyIdGetter)
            throws SQLException {
        if (communityId == null) {
            list.clear();
            return;
        }
        Map<Integer, Integer> map = elderlyCommunityMap();
        list.removeIf(t -> {
            Integer eid = elderlyIdGetter.apply(t);
            if (eid == null) return true;
            Integer cid = map.get(eid);
            return cid == null || cid != communityId;
        });
    }
    public static <T> void keepOwnCommunityByField(List<T> list, Integer communityId, Function<T, Integer> communityIdGetter) {
        if (communityId == null) {
            list.clear();
            return;
        }
        list.removeIf(t -> {
            Integer cid = communityIdGetter.apply(t);
            return cid == null || cid != communityId;
        });
    }
}

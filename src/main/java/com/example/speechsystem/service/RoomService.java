package cn.nullcat.sckj.service;

import cn.nullcat.sckj.pojo.DTO.RoomEvaluationDTO;
import cn.nullcat.sckj.pojo.PageBean;
import cn.nullcat.sckj.pojo.Room;

import java.util.List;

public interface RoomService {
    PageBean getAllRomms(Integer page, Integer pageSize, String name, String location, Integer capacity, Integer status);

    Room getById(Integer id);

    void addRoom(Room room);

    void updateRoom(Room room);

    void deleteById(Integer id);

    /**
     * 根据评估参数查找合适的会议室
     * @param evaluationDTO 评估参数
     * @return 符合条件的会议室列表
     */
    List<Room> findAvailableRoomsByRequirements(RoomEvaluationDTO evaluationDTO);
}

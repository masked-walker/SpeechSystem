package com.example.speechsystem.mapper;

import com.example.speechsystem.pojo.Room;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface RoomMapper {
    /**
     * 分页条件获取会议室列表
     * @param name
     * @param location
     * @param capacity
     * @param status
     * @return
     */
    List<Room> getAllRomms(String name, String location, Integer capacity, Integer status);

    /**
     * 根据id获取会议室详情
     * @param id
     * @return
     */
    @Select("select * from room where id = #{id}")
    Room getById(Integer id);

    /**
     * 新增会议室
     * @param room
     */
    @Insert("insert into " +
            "room(name, location, capacity, equipment, description, status, create_time, update_time, is_deleted) " +
            "VALUES(#{name},#{location},#{capacity},#{equipment},#{description},#{status},#{createTime},#{updateTime},#{isDeleted}) ")
    void addRoom(Room room);

    /**
     * 修改会议室
     * @param room
     */
    @Update("update room set" +
            " name = #{name}," +
            "location = #{location}," +
            "capacity = #{capacity}," +
            "equipment = #{equipment}," +
            "description = #{description}," +
            "status = #{status}," +
            "update_time = NOW()" +
            "where id = #{id}")
    void updateRoom(Room room);

    /**
     * 删除会议室
     * @param id
     */
    @Delete("delete from room where id =#{id}")
    void deleteById(Integer id);

    /**
     * 根據時間、人數和設備要求查詢合適的會議室
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param requiredCapacity 所需容量
     * @param requiredEquipment 所需設備列表
     * @return 符合條件的會議室列表
     */
    List<Room> findSuitableRooms(@Param("startTime") Date startTime,
                                @Param("endTime") Date endTime,
                                @Param("requiredCapacity") Integer requiredCapacity,
                                @Param("requiredEquipment") List<String> requiredEquipment);

    /**
     * 根据时间、人数和设备要求查找合适的会议室
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param requiredCapacity 所需容量
     * @param requiredEquipment 所需设备列表
     * @return 符合条件的会议室列表
     */
    List<Room> findAvailableRoomsByRequirements(@Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime,
                                               @Param("requiredCapacity") Integer requiredCapacity,
                                               @Param("requiredEquipment") List<String> requiredEquipment);
}

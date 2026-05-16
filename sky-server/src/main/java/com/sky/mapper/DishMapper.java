package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);



    //@Options()
    @AutoFill(value= OperationType.INSERT)
    void insert(Dish dish);


    Page<DishVO> list(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    void deleteBatch(List<Long> ids);

    //回显菜品详情
    DishVO getInfoById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateById(Dish dish);

    List<Dish> getByCategoryId(Dish dish);

    @Select("select a.* from dish a left join setmeal_dish sd on a.id = sd.dish_id where sd.setmeal_id = #{id}")
    List<Dish> getBySetmealId(Long id);

    //根据分类id查询菜品，并查询口味
    List<DishVO> getDishWithFlavorByCategoryId(Dish dish);
}

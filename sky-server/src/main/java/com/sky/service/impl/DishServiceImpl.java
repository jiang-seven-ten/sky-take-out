package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        List<DishFlavor> flavorList=dishDTO.getFlavors();
        if(flavorList!=null&&!flavorList.isEmpty()){
            flavorList.forEach(fl->{
                fl.setDishId(dish.getId());
            });
        }
        dishFlavorMapper.insertBatch(flavorList);
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult<DishVO> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> p=dishMapper.list(dishPageQueryDTO);

        return new PageResult<DishVO>(p.getTotal(),p.getResult());

    }



    /**
     * 删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        log.info("删除菜品：{}", ids);

        //1、判断菜品是否在售
        for(Long id :ids){
            Dish dish =dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //2、判断菜品是否被关联到套餐
        List<Long> setmealIds=setmealDishMapper.getSetmealIdByDishId(ids);
        if(!setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //如果菜品没有被关联到套餐并且不是在售状态，才能删除
        //3、删除菜品
        dishMapper.deleteBatch(ids);

        //4、删除菜品口味
        dishFlavorMapper.deleteBatch(ids);
    }



    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getInfoById(Long id) {
        DishVO dishVO=dishMapper.getInfoById(id);
        return dishVO;
    }


    /**
     * 更新菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        log.info("更新菜品：{}", dishDTO);

        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateById(dish);
        //删除菜品口味
        dishFlavorMapper.deleteBatch(Arrays.asList(dishDTO.getId()));

        List<DishFlavor> flavorList=dishDTO.getFlavors();

        if(flavorList!=null&&!flavorList.isEmpty()){
            flavorList.forEach(fl->{
                fl.setDishId(dish.getId());
            });
        }
        dishFlavorMapper.insertBatch(flavorList);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        Dish dish=Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.DISABLE)
                .build();
        return dishMapper.getByCategoryId(dish);
    }


}

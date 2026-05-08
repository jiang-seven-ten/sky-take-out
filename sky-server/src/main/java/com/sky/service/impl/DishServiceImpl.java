package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);

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
}

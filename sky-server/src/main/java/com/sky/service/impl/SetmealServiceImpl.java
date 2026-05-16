package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        Setmeal setmeal =new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();

        for(SetmealDish sd:setmealDishes){
            sd.setSetmealId(setmeal.getId());
        }

        setmealDishMapper.insertBatch(setmealDishes);

    }

    @Override
    public PageResult<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> list=setmealMapper.page(setmealPageQueryDTO);

        return new PageResult(list.getTotal(),list.getResult());
    }


    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {

        ids.forEach(id->{
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()==StatusConstant.ENABLE ){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        setmealMapper.deleteBatchIds(ids);
        setmealDishMapper.deleteBatch(ids);
    }




    @Override
    public SetmealVO getInfoById(Long id) {
        return setmealMapper.getInfoById(id);
    }

    @Transactional
    @Override
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal =new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBatch(Arrays.asList(setmealDTO.getId()));

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();

        if(!setmealDishes.isEmpty()) {
            for (SetmealDish sd : setmealDishes) {
                sd.setSetmealId(setmealDTO.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if(status==StatusConstant.ENABLE){
            List<Dish> dishList=dishMapper.getBySetmealId(id);
            if(!dishList.isEmpty()){
                dishList.forEach(d->{
                   if(d.getStatus()==StatusConstant.DISABLE){
                       throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                   }
                });
            }
        }

        Setmeal setmeal=Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setmealMapper.update(setmeal);
    }




    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }



    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }



}

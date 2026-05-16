package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;


    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //新增菜品后，删除缓存中的菜品列表
        String key="dish_"+dishDTO.getCategoryId();
        redisTemplate.delete(key);


        return Result.success();
    }



    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult<DishVO> pageResult=dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品：{}", ids);
        dishService.delete(ids);

        //删除菜品后，删除缓存中所有的菜品列表(以dish_开头)
        cleanCache("dish_*");

        return Result.success();
    }



    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getInfoById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO=dishService.getInfoById(id);
        return Result.success(dishVO);
    }


    /**
     * 更新菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //更新菜品后，删除缓存中的菜品列表
        cleanCache("dish_*");

        return Result.success();
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        log.info("新增套餐菜品时，根据分类id查询菜品，但不查询口味：{}", categoryId);
        List<Dish> list=dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }




    /**
     * 菜品的起售和停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status,Long id) {
        log.info("菜品的起售和停售：{} {}", status,id);
        dishService.startOrStop(status, id);

        //更新菜品状态后，删除缓存中的菜品列表
        cleanCache("dish_*");

        return Result.success();
    }


    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }






}

package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @CacheEvict(cacheNames="setmealCache",key="#setmealDTO.categoryId")
    public Result saveWithDish(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }


    @GetMapping("/page")
    public Result<PageResult<SetmealVO>> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}", setmealPageQueryDTO);
        PageResult<SetmealVO> page=setmealService.page(setmealPageQueryDTO);
        return Result.success(page);
    }


    @DeleteMapping
    @CacheEvict(cacheNames="setmealCache",allEntries=true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除套餐根据ids：{}", ids);
        setmealService.deleteByIds(ids);
        return Result.success();
    }



    @GetMapping("/{id}")
    public Result<SetmealVO> getInfoById(@PathVariable Long id){
        log.info("根据id查询回显信息{}",id);
        SetmealVO setmealVO=setmealService.getInfoById(id);
        return Result.success(setmealVO);
    }


    @PutMapping
    @CacheEvict(cacheNames="setmealCache",allEntries = true)
    public Result updateWithDish(@RequestBody SetmealDTO setmealDTO){
        log.info("更新套餐：{}", setmealDTO);
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }


    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames="setmealCache",allEntries=true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("更新套餐状态：{},{}", status,id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }

}

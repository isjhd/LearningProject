package com.isjhd.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isjhd.reggie.dto.SetmealDto;
import com.isjhd.reggie.entity.Setmeal;

import java.util.List;

/* @author  i-s-j-h-d
 * @version 1.0 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    //起售、停售
    public void selling(List<Long> ids, Long status);

}

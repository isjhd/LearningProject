package com.isjhd.reggie.dto;


import com.isjhd.reggie.entity.Setmeal;
import com.isjhd.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

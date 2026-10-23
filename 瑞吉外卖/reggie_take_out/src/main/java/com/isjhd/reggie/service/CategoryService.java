package com.isjhd.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isjhd.reggie.entity.Category;

/* @author  i-s-j-h-d
 * @version 1.0 */
public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}

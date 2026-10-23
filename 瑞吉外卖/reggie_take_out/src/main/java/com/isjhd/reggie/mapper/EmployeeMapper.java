package com.isjhd.reggie.mapper;

/* @author  i-s-j-h-d
 * @version 1.0 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isjhd.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}

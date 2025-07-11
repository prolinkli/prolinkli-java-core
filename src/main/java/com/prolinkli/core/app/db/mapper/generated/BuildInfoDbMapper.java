package com.prolinkli.core.app.db.mapper.generated;

import com.prolinkli.core.app.db.model.generated.BuildInfoDb;
import com.prolinkli.core.app.db.model.generated.BuildInfoDbExample;
import com.prolinkli.core.app.db.model.generated.BuildInfoDbKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BuildInfoDbMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    long countByExample(BuildInfoDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int deleteByExample(BuildInfoDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int deleteByPrimaryKey(BuildInfoDbKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int insert(BuildInfoDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int insertSelective(BuildInfoDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    List<BuildInfoDb> selectByExample(BuildInfoDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    BuildInfoDb selectByPrimaryKey(BuildInfoDbKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int updateByExampleSelective(@Param("row") BuildInfoDb row, @Param("example") BuildInfoDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int updateByExample(@Param("row") BuildInfoDb row, @Param("example") BuildInfoDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int updateByPrimaryKeySelective(BuildInfoDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.build_info
     *
     * @mbg.generated Sat Jun 28 12:29:39 EDT 2025
     */
    int updateByPrimaryKey(BuildInfoDb row);
}
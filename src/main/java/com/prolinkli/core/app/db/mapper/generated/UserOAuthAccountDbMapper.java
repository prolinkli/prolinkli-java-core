package com.prolinkli.core.app.db.mapper.generated;

import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDb;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbExample;
import com.prolinkli.core.app.db.model.generated.UserOAuthAccountDbKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserOAuthAccountDbMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    long countByExample(UserOAuthAccountDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int deleteByExample(UserOAuthAccountDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int deleteByPrimaryKey(UserOAuthAccountDbKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int insert(UserOAuthAccountDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int insertSelective(UserOAuthAccountDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    List<UserOAuthAccountDb> selectByExample(UserOAuthAccountDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    UserOAuthAccountDb selectByPrimaryKey(UserOAuthAccountDbKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int updateByExampleSelective(@Param("row") UserOAuthAccountDb row, @Param("example") UserOAuthAccountDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int updateByExample(@Param("row") UserOAuthAccountDb row, @Param("example") UserOAuthAccountDbExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int updateByPrimaryKeySelective(UserOAuthAccountDb row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table public.user_oauth_accounts
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    int updateByPrimaryKey(UserOAuthAccountDb row);
}
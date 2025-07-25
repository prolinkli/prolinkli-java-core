package com.prolinkli.core.app.db.model.generated;

import java.util.Date;

public class UserOAuthAccountDb extends UserOAuthAccountDbKey {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.oauth_user_id
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private String oauthUserId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.display_name
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private String displayName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.profile_picture_url
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private String profilePictureUrl;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.locale
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private String locale;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.created_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private Date createdAt;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column public.user_oauth_accounts.updated_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    private Date updatedAt;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.oauth_user_id
     *
     * @return the value of public.user_oauth_accounts.oauth_user_id
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public String getOauthUserId() {
        return oauthUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.oauth_user_id
     *
     * @param oauthUserId the value for public.user_oauth_accounts.oauth_user_id
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setOauthUserId(String oauthUserId) {
        this.oauthUserId = oauthUserId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.display_name
     *
     * @return the value of public.user_oauth_accounts.display_name
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.display_name
     *
     * @param displayName the value for public.user_oauth_accounts.display_name
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.profile_picture_url
     *
     * @return the value of public.user_oauth_accounts.profile_picture_url
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.profile_picture_url
     *
     * @param profilePictureUrl the value for public.user_oauth_accounts.profile_picture_url
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.locale
     *
     * @return the value of public.user_oauth_accounts.locale
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public String getLocale() {
        return locale;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.locale
     *
     * @param locale the value for public.user_oauth_accounts.locale
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.created_at
     *
     * @return the value of public.user_oauth_accounts.created_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.created_at
     *
     * @param createdAt the value for public.user_oauth_accounts.created_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column public.user_oauth_accounts.updated_at
     *
     * @return the value of public.user_oauth_accounts.updated_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column public.user_oauth_accounts.updated_at
     *
     * @param updatedAt the value for public.user_oauth_accounts.updated_at
     *
     * @mbg.generated Sat Jun 28 22:07:42 EDT 2025
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
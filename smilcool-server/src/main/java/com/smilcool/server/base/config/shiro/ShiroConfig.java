package com.smilcool.server.base.config.shiro;

import com.smilcool.server.base.config.shiro.filter.CustomFormAuthenticationFilter;
import com.smilcool.server.base.config.shiro.filter.CustomHttpMethodPermissionFilter;
import com.smilcool.server.base.config.shiro.filter.CustomPermissionsAuthorizationFilter;
import com.smilcool.server.base.config.shiro.filter.CustomRolesAuthorizationFilter;
import com.smilcool.server.base.config.shiro.realm.CustomAuthorizingRealm;
import com.smilcool.server.core.pojo.po.RuleMap;
import com.smilcool.server.core.service.RuleMapService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Angus
 * @date 2019/4/5
 */
@Slf4j
@Configuration
public class ShiroConfig {

    @Autowired
    private RuleMapService ruleMapService;

    /**
     * 设置自定义 Realm
     *
     * @return
     */
    @Bean
    public Realm realm() {
        return new CustomAuthorizingRealm();
    }

    /**
     * 为替换原过滤器，需要通过 shiroFilterFactoryBean 进行设置。不替换时，
     * 可简化为配置 shiroFilterChainDefinition，自定义过滤器采用 Bean 注入
     * 简化配置参考官网：https://shiro.apache.org/spring-boot.html
     *
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 设置过滤器
        Map<String, Filter> filters = shiroFilterFactoryBean.getFilters();
        filters.put("authc", authc());
        filters.put("perms", perms());
        filters.put("rest", rest());
        filters.put("roles", roles());
        // 配置过滤器链映射
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 从数据库获取规则映射
        List<RuleMap> ruleMapList = ruleMapService.getRuleMapList();
        ruleMapList.forEach(ruleMap -> filterChainDefinitionMap.put(ruleMap.getUrl(), buildRule(ruleMap)));
        log.info("filterChainDefinitionMap: {}", filterChainDefinitionMap);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 构建过滤规则
     *
     * @param ruleMap
     * @return
     */
    private String buildRule(RuleMap ruleMap) {
        StringBuilder rule = new StringBuilder();
        // 是否需要身份验证
        if (ruleMap.getAuthc()) {
            rule.append("authc");
        } else {
            rule.append("anon");
        }
        // 是否需要角色验证
        if (ruleMap.getUseRoles()) {
            rule.append(",roles[").append(ruleMap.getRoles()).append("]");
        }
        // 是否需要权限验证
        if (ruleMap.getUsePerms()) {
            rule.append(",perms[").append(ruleMap.getPerms()).append("]");
        }
        // 是否需要 HTTP 方法（REST）验证
        if (ruleMap.getUseRest()) {
            rule.append(",rest[").append(ruleMap.getRest()).append("]");
        }
        return rule.toString();
    }

    /**
     * 开启缓存
     *
     * @return
     */
    @Bean
    protected CacheManager cacheManager() {
        return new MemoryConstrainedCacheManager();
    }

    /**
     * 使用 Bean 注入时，原过滤器仍然生效，页面依旧会跳转到 /login.jsp，无法达到替代效果
     *
     * @return
     */
    private FormAuthenticationFilter authc() {
        return new CustomFormAuthenticationFilter();
    }

    private RolesAuthorizationFilter roles() {
        return new CustomRolesAuthorizationFilter();
    }

    private PermissionsAuthorizationFilter perms() {
        return new CustomPermissionsAuthorizationFilter();
    }

    private HttpMethodPermissionFilter rest() {
        return new CustomHttpMethodPermissionFilter();
    }
}
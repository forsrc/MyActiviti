package org.activiti.rest.conf;

import org.activiti.engine.*;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.rest.form.MonthFormType;
import org.activiti.rest.form.ProcessDefinitionFormType;
import org.activiti.rest.form.UserFormType;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

//@Configuration
public class ActivitiEngineConfiguration {

    private final Logger log = LoggerFactory.getLogger(ActivitiEngineConfiguration.class);

    @Autowired
    protected Environment environment;

    @Autowired
    protected DataSource dataSource;
  
  /*@Bean
  public DataSource dataSource() { 
    SimpleDriverDataSource ds = new SimpleDriverDataSource();
    
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Driver> driverClass = (Class<? extends Driver>) Class.forName(environment.getProperty("jdbc.driver", "org.h2.Driver"));
      ds.setDriverClass(driverClass);
      
    } catch (Exception e) {
      log.error("Error loading driver class", e);
    }
    
    // Connection settings
    ds.setUrl(environment.getProperty("jdbc.url", "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000"));
    ds.setUsername(environment.getProperty("jdbc.username", "sa"));
    ds.setPassword(environment.getProperty("jdbc.password", ""));
    
    return ds;
  }*/

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean(name = "processEngineFactoryBean")
    public ProcessEngineFactoryBean processEngineFactoryBean() {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean;
    }

    @Bean(name = "processEngine")
    public ProcessEngine processEngine() {
        // Safe to call the getObject() on the @Bean annotated processEngineFactoryBean(), will be
        // the fully initialized object instanced from the factory and will NOT be created more than once
        try {
            return processEngineFactoryBean().getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "processEngineConfiguration")
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(environment.getProperty("engine.schema.update", "true"));
        processEngineConfiguration.setTransactionManager(annotationDrivenTransactionManager());
        processEngineConfiguration.setJobExecutorActivate(Boolean.valueOf(
                environment.getProperty("engine.activate.jobexecutor", "false")));
        processEngineConfiguration.setAsyncExecutorEnabled(Boolean.valueOf(
                environment.getProperty("engine.asyncexecutor.enabled", "true")));
        processEngineConfiguration.setAsyncExecutorActivate(Boolean.valueOf(
                environment.getProperty("engine.asyncexecutor.activate", "true")));
        processEngineConfiguration.setHistory(environment.getProperty("engine.history.level", "full"));

        List<AbstractFormType> formTypes = new ArrayList<AbstractFormType>();
        formTypes.add(new UserFormType());
        formTypes.add(new ProcessDefinitionFormType());
        formTypes.add(new MonthFormType());
        processEngineConfiguration.setCustomFormTypes(formTypes);

        return processEngineConfiguration;
    }

    @Bean
    public RepositoryService repositoryService() {
        return processEngine().getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() {
        return processEngine().getTaskService();
    }

    @Bean
    public HistoryService historyService() {
        return processEngine().getHistoryService();
    }

    @Bean
    public FormService formService() {
        return processEngine().getFormService();
    }

    @Bean
    public IdentityService identityService() {
        return processEngine().getIdentityService();
    }

    @Bean
    public ManagementService managementService() {
        return processEngine().getManagementService();
    }
}

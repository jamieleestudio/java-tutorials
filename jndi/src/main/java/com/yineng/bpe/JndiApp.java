package com.yineng.bpe;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class JndiApp {


    public static class User{
        private String name;

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static void main( String[] args ) throws NamingException {

//        JndiTemplate jndiTemplate = new JndiTemplate();
//        ctx = (InitialContext) jndiTemplate.getContext();
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        props.put(Context.PROVIDER_URL, "jnp://localhost:1099");

        final InitialContext context = new InitialContext(props);
        context.bind("user-li", new User("lixiaofeng"));

        User user = (User) context.lookup("user-li");
        System.out.println(user.getName());
    }

}


import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Eval {

    private static String ims_str;
    private static Map<String, Integer> ims_map;
    private static Set<String> keys_ims;

    //"ims"="3c+b+a" ==> GLOBAL INITIAL CAPACITY    
    public Eval(String ims_){
        ims_str = ims_;
        ims_map = string2map(ims_);
        keys_ims = ims_map.keySet();
    }
    

    //f1,f2 are of the form "a" or "!a"
    //Is one the negated or the other?
    private static boolean negated(String f1, String f2) {
        return
            (f1.charAt(0) == '!' && f2.charAt(0) != '!' && f1.substring(1).equals(f2))
            ||
            (f2.charAt(0) == '!' && f1.charAt(0) != '!' && f2.substring(1).equals(f1));
    }

    static final String SEP = "'";

    //token is of the form "2/c,a,10/!y_3", and we assume non-negative coefficients
    //returns a dictionary representing a multi-set
    public static Map<String,Integer> string2map(String token) {
        String[] valC = token.split(",");
        Map<String,Integer> res = new HashMap<String,Integer>();

        for(int i=0;i<valC.length;i++) {
            String[] term = valC[i].split(SEP);
            
            if (term.length == 1) { //"a"
                res.put(term[0],1);
            }
            else {
                res.put(term[1],Integer.parseInt(term[0]));
            }
        }
        return res;
    }

    //"mapTuple" is not empty. It represents a multi-set
    //The value of each key is assumed to be > 0
    public static String map2string(Map<String,Integer> mapTuple) {
        Set<String> keys = mapTuple.keySet();
        Iterator<String> it = keys.iterator();
        String res = "";
        String k = it.next();

        res += mapTuple.get(k) + SEP + k;

        while(it.hasNext()) {
            k = it.next();
            if(mapTuple.get(k) != 0) {
                res += "," + mapTuple.get(k) + SEP + k;
            }
        }
        return res;
    }

    //"e1","e2" are of the form "a,b,!c,d,...", representing the AND of the elements, or "1"
    //they are assumed to be non-selfcontradicting,
    //which means that neither "e1" nor "e2" contain "a" and "!a" simultaneously
    //
    //Is there any contradiction between elements of "e1" and "e2"?


    public static boolean synchro(String buchi, String tuple, String capacity, String...args)
    //args of even index : robot formula, args of uneven index : condition of the cap 
    //Function which be executed first if there is 1 robot or more (unknown number)
    //First, initialization of the maps and of the different objects
    //Then, change of the observation tuple considering the transitions
    {
        Set<String> b = new HashSet<String>(Arrays.asList(buchi.split(",")));
        Set<String> s = new HashSet<String>();
        int x1=0;
        int x2=0;
        int k=0;
        String str_t = tuple;
        String str_c = capacity;
        String r_cond = args[0];

        for (String arg : args){
            if (k%2 ==0){
                Set<String> r = new HashSet<String>(Arrays.asList(arg.split(",")));
                s.addAll(r);
                r_cond = arg;
            }
            else{
                str_c = inc_c(str_c, r_cond, arg); //We need the robot cond to have access to the "size" of the robot in the capacity
                str_t = inc_t(str_t, arg);
            }
            k+=1;
        }

        Map<String,Integer> map_t;
        map_t = string2map(str_t);
        Set<String> keys_t = map_t.keySet();
        Iterator<String> it_t = keys_t.iterator();
        Map<String,Integer> map_c;
        map_c = string2map(str_c);
        Set<String> keys_c = map_c.keySet();
        Iterator<String> it_c = keys_c.iterator();

        //I think this is not useful : cannot happen with a good working example
        while(it_t.hasNext()){
            String next = it_t.next();
            if (map_t.get(next)<0 ){
                return false;
            }
        }
        //Same for second condition
        while(it_c.hasNext()){
            String next = it_c.next();  
            if (map_c.get(next)<0 || map_c.get(next)>ims_map.get(next)){
                return false;
            }
        }

        //ims_map.get(next)

        // Verify if the buchi is not in contradiction with the entry tuple (eg: no robots in a if !a) ==> METHOD 1
        while(it_t.hasNext()){
            String next = it_t.next();
            String el = "!" + next;
            if (b.contains(el) && !map_t.get(next).equals(0)){
                return false;
            }
        }


        for(String s1 : b) {
            if(s1.charAt(0) != '!'){
                x1=x1+1;
            }
        }

        if (buchi.equals("1")) {
            return true;
        }
        else {
            for(String buc : b) {
                for(String rob : s) {
                    if(negated(buc,rob)) {
                        return false;
                    }
                    else if(buc.charAt(0) != '!' && buc.equals(rob)){
                        x2=x2+1;
                    }
                }
            }
        }

        if(x1==x2){
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1)
    {
        String[] args = new String[]{robot1, cond1};
        return synchro(buchi, tuple, capacity, args);
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1, String robot2, String cond2)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2};
        return synchro(buchi, tuple, capacity, args);
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3};
        return synchro(buchi, tuple, capacity, args);
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4};
        return synchro(buchi, tuple, capacity, args);
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4, String robot5, String cond5)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4,robot5, cond5};
        return synchro(buchi, tuple, capacity, args);
    }

    public static boolean synchro(String buchi, String tuple, String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4, String robot5, String cond5, String robot6, String cond6)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4,robot5, cond5, robot6, cond6};
        return synchro(buchi, tuple, capacity, args);
    }
    
    //for any key, the associated value should have to be >=0
    //if value<0, |value|<= the value associated to "obs"
    //multi_set="2/a,-1/c"

    public static String inc_t(String tuple, String cond){
        Map<String,Integer> map_t = string2map(tuple);
        Map<String,Integer> map_cond = string2map(cond);
        Set<String> keys_cond = map_cond.keySet();
        Iterator<String> it_cond = keys_cond.iterator();

        while (it_cond.hasNext()){
            String next = it_cond.next();
            if(!map_t.containsKey(next)) {
                map_t.put(next,map_cond.get(next));
            }
            else{
                map_t.put(next,map_t.get(next)+map_cond.get(next));
            }
        }

        return map2string(map_t);
    }

    public static String inc_t(String tuple, String... args){
        String t_new = tuple;
        for(String arg : args){
            t_new = inc_t(t_new, arg);
        }
        return t_new;
    }


    public static String inc_t(String tuple, String cond1,String cond2)
    {
        String[] args = new String[]{ cond1, cond2};
        return inc_t(tuple, args);
    }

    public static String inc_t(String tuple, String cond1,String cond2,  String cond3)
    {
        String[] args = new String[]{cond1, cond2, cond3};
        return inc_t(tuple, args);
    }

    public static String inc_t(String tuple, String cond1,String cond2,  String cond3, String cond4)
    {
        String[] args = new String[]{cond1, cond2, cond3, cond4};
        return inc_t(tuple, args);
    }

    public static String inc_t(String tuple, String cond1,String cond2,  String cond3, String cond4,  String cond5)
    {
        String[] args = new String[]{cond1, cond2, cond3, cond4, cond5};
        return inc_t(tuple, args);
    }

    public static String inc_t(String tuple,String cond1,String cond2,  String cond3, String cond4,  String cond5,  String cond6)
    {
        String[] args = new String[]{cond1, cond2, cond3, cond4, cond5, cond6};
        return inc_t(tuple, args);
    }

    public static String inc_c(String capacity, String robot, String cond){
        Map<String,Integer> map_c = string2map(capacity);
        Map<String,Integer> map_mt = string2map(cond);
        Set<String> keys_ms = map_mt.keySet();
        Set<String> r = new HashSet<String>(Arrays.asList(robot.split(",")));
        int size = map_mt.get(keys_ms.toArray()[0]);
        Set<String> s_post = new HashSet<String>();
        Set<String> s_pre = new HashSet<String>();

        for (String rob : r){
            if (rob.charAt(0) != '!'){
                if (!map_mt.containsKey(rob)){
                    s_pre.add(rob);
                }
                s_post.add(rob);
            }
            else{
                 s_pre.add(String.valueOf(rob.charAt(1)));
            }
        }

        List<String> s_pre_l = new ArrayList<String>();
        s_pre_l.addAll(s_pre);
        List<String> s_post_l = new ArrayList<String>();
        s_post_l.addAll(s_post);

        Collections.sort(s_pre_l);
        Collections.sort(s_post_l);

        String s_pre_str = null;
        String s_post_str= null;
        int k_pre=0;
        int k_post =0;

        if (!s_pre_l.isEmpty()){
            for( String pre : s_pre_l){
                if (k_pre==0){
                    s_pre_str = pre;
                }
                else{
                    s_pre_str+= "." + pre; 
                }
                k_pre+=1;
            }
            int init_pre = 0;
            if (map_c.containsKey(s_pre_str)){
                init_pre = map_c.get(s_pre_str);
            }
            map_c.put(s_pre_str,init_pre+Math.abs(size));
        }

        if (!s_post_l.isEmpty()) {
            for( String post : s_post_l){
                if (k_post==0){
                    s_post_str= post;
                }
                else{
                    s_post_str+= "." + post; 
                }
                k_post+=1;
            }
            int init_post = 0;
            if (map_c.containsKey(s_post_str)){
                init_post = map_c.get(s_post_str);
            }
            map_c.put(s_post_str,init_post-Math.abs(size));
        }
        return map2string(map_c);
    }

    public static String inc_c(String capacity, String... args){
        int k=0;
        String robot = args[0];
        String new_c = capacity;

        for (String arg : args){
            if (k%2 ==0){
                robot = arg;
            }
            else{
                new_c = inc_c(new_c, robot, arg);
            }
            k+=1;
        }
        return new_c;
    }

    public static String inc_c(String capacity, String robot1, String cond1, String robot2, String cond2)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2};
        return inc_c(capacity, args);
    }

    public static String inc_c(String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3};
        return inc_c(capacity, args);
    }

    public static String inc_c(String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4};
        return inc_c(capacity, args);
    }

    public static String inc_c(String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4, String robot5, String cond5)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4,robot5, cond5};
        return inc_c(capacity, args);
    }

    public static String inc_c(String capacity, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3, String robot4, String cond4, String robot5, String cond5, String robot6, String cond6)
    {
        String[] args = new String[]{robot1, cond1,robot2, cond2,robot3, cond3,robot4, cond4,robot5, cond5, robot6, cond6};
        return inc_c(capacity, args);
    }


    //just for testing
     public static void main(String[] arg) {
         Scanner terminalInput = new Scanner(System.in);
         String bu,rob1,cond1,tup,rob2,cond2,rob3,cond3, capacity;

         System.out.print("bu: ");
         bu = terminalInput.nextLine();
         while (bu.length() != 0) {
             System.out.print("rob1: ");
             rob1 = terminalInput.nextLine();
             System.out.print("cond1: ");
             cond1 = terminalInput.nextLine();
             System.out.print("rob2: ");
             rob2 = terminalInput.nextLine();
             System.out.print("cond2: ");
             cond2 = terminalInput.nextLine();
             System.out.print("cap: ");
             capacity = terminalInput.nextLine();
             System.out.print("tup: ");
             tup = terminalInput.nextLine();
             System.out.println(synchro(bu,tup,capacity,rob1,cond1,rob2, cond2));
             System.out.println(inc_t(tup,cond1,cond2));
             System.out.println(inc_c(capacity,rob1,cond1,rob2,cond2));
             System.out.print("bu: ");
             bu = terminalInput.nextLine();
         }
     }
}
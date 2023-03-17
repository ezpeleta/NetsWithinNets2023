//Imports
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


    public static String cap_str;                  //current global capacity (string)
    private static Map<String, Integer> cap_map_init;    //Initial global capacity (map) 
    private static String log_str;                  //Initial logical state multiset (string)

    static final String SEP = "'";                  //Separation element for multisets

    //Initialization function with initial global capacity & initial logic state multiset
    public Eval(String cap, String log) {
        cap_str = cap;
        cap_map_init = string2map(cap);
        log_str = log;
    }

    //Function to verify that to formulas do not contradict each other (regarding negations)
    //f1,f2 are of the form "a" or "!a"
    private static boolean negated(String f1, String f2) {
        return
            (f1.charAt(0) == '!' && f2.charAt(0) != '!' && f1.substring(1).equals(f2))
            ||
            (f2.charAt(0) == '!' && f1.charAt(0) != '!' && f2.substring(1).equals(f1));
    }

    //Returns a map of the considered String multiset
    //Multiset are of forms "1'a,2'b" for example (depending on SEP and the form of separation, here a comma)
    public static Map<String,Integer> string2map(String token) {
        String[] valC = token.split(",");
        Map<String,Integer> res = new HashMap<String,Integer>();

        for(int i=0; i<valC.length; i++) {
            String[] term = valC[i].split(SEP);

            if (term.length == 1) { //"a"
                res.put(term[0],1);
            } else {
                res.put(term[1],Integer.parseInt(term[0]));
            }
        }
        return res;
    }

    //"mapTuple" is not empty. It represents a multi-set
    //The value of each key is assumed to be > 0
    //Returns a string of the considered Map multiset
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

    //This function takes an unknown number of entries : for the unknown number of robots that can be taken into account within a
    //System Net transition.
    public static boolean synchro(String buchi, String...args)
    //args of even index : robot formula, args of uneven index : condition of the cap
    //Function which be executed first if there is 1 robot or more (unknown number)
    {
        //Initialization
        Set<String> b = new HashSet<String>(Arrays.asList(buchi.split(",")));
        Set<String> s = new HashSet<String>();
        int x1=0;
        int x2=0;
        int k=0;
        String r_cond = args[0];
        String log_str_current = log_str;
        String cap_str_current = cap_str;
        String str_c = "";
        String str_t = "";

        for (String arg : args) {
            if (k%2 ==0) {
                Set<String> r = new HashSet<String>(Arrays.asList(arg.split(",")));
                s.addAll(r);
                r_cond = arg;
            } else {
                inc_c(r_cond, arg); //We need the robot cond to have access to the "size" of the robot in the capacity
                inc_t( arg);
                str_c = cap_str;
                str_t = log_str;
            }
            k+=1;
        }

        cap_str = cap_str_current;
        log_str = log_str_current;

        //Map initializations
        Map<String,Integer> map_t;
        map_t = string2map(str_t);
        Set<String> keys_t = map_t.keySet();
        Iterator<String> it_t = keys_t.iterator();
        Map<String,Integer> map_c;
        map_c = string2map(str_c);
        Set<String> keys_c = map_c.keySet();
        Iterator<String> it_c = keys_c.iterator();

        //As a security, we verify that the logical state multiset does not have negative elements
        while(it_t.hasNext()) {
            String next = it_t.next();
            if (map_t.get(next)<0 ) {
                return false;
            }
        }
        //We verify that the capacity multiset does not have negative elements (condition on full capacity)
        while(it_c.hasNext()) {
            String next = it_c.next();
            if (map_c.get(next)<0 || map_c.get(next)>cap_map_init.get(next)) {
                return false;
            }
        }

        // Verifies if the buchi is not in contradiction with the entry logical state (eg: no robots in a if !a)
        while(it_t.hasNext()) {
            String next = it_t.next();
            String el = "!" + next;
            if (b.contains(el) && !map_t.get(next).equals(0)) {
                return false;
            }
        }
        //Counts the number of not negated elements in the buchi formula
        for(String s1 : b) {
            if(s1.charAt(0) != '!') {
                x1=x1+1;
            }
        }

        //Checks the verification of the buchi formula by robots movements
        if (buchi.equals("1")) {
            return true;
        } else {
            for(String buc : b) {
                for(String rob : s) {
                    if(negated(buc,rob)) {      //Verifies incompatibility
                        return false;
                    } else if(buc.charAt(0) != '!' && buc.equals(rob)) {
                        x2=x2+1;
                    }
                }
            }
        }

        if(x1==x2) {
            return true;
        } else {
            return false;
        }
    }

    //However, the Renew Software does not support it, so we are obliged to adapt the entry depending on the number of robots
    public static boolean synchro(String buchi, String robot1, String cond1) {
        String[] args = new String[] {robot1, cond1};
        return synchro(buchi, args);
    }

    public static boolean synchro(String buchi, String robot1, String cond1, String robot2, String cond2) {
        String[] args = new String[] {robot1, cond1,robot2, cond2};
        return synchro(buchi, args);
    }

    public static boolean synchro(String buchi, String robot1, String cond1, String robot2, String cond2, String robot3, String cond3) {
        String[] args = new String[] {robot1, cond1,robot2, cond2,robot3, cond3};
        return synchro(buchi, args);
    }



    //Function updating the logical state multiset
    public static void inc_t(String cond) {
        Map<String,Integer> map_t = string2map(log_str);
        Map<String,Integer> map_cond = string2map(cond);
        Set<String> keys_cond = map_cond.keySet();
        Iterator<String> it_cond = keys_cond.iterator();

        while (it_cond.hasNext()) {
            String next = it_cond.next();
            if(!map_t.containsKey(next)) {
                map_t.put(next,map_cond.get(next));
            } else {
                map_t.put(next,map_t.get(next)+map_cond.get(next));
            }
        }

        log_str = map2string(map_t);
    }

    //This function takes an unknown number of entries : for the unknown number of robots that can be taken into account within a
    //System Net transition.
    public static void inc_t(String... args) {
        for(String arg : args) {
            inc_t(arg);
        }
    }

    //However, the Renew Software does not support it, so we are obliged to adapt the entry depending on the number of robots
    public static void inc_t(String cond1,String cond2) {
        String[] args = new String[] { cond1, cond2};
        inc_t(args);
    }

    public static void inc_t(String cond1,String cond2,  String cond3) {
        String[] args = new String[] {cond1, cond2, cond3};
        inc_t(args);
    }


    public static void inc_c(String robot, String cond) {
        Map<String,Integer> map_c = string2map(cap_str);
        Map<String,Integer> map_mt = string2map(cond);
        Set<String> keys_ms = map_mt.keySet();
        Set<String> r = new HashSet<String>(Arrays.asList(robot.split(",")));
        int size = map_mt.get(keys_ms.toArray()[0]);
        Set<String> s_post = new HashSet<String>();
        Set<String> s_pre = new HashSet<String>();

        for (String rob : r) {
            if (rob.charAt(0) != '!') {
                if (!map_mt.containsKey(rob)) {
                    s_pre.add(rob);
                }
                s_post.add(rob);
            } else {
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

        if (!s_pre_l.isEmpty()) {
            for( String pre : s_pre_l) {
                if (k_pre==0) {
                    s_pre_str = pre;
                } else {
                    s_pre_str+= "." + pre;
                }
                k_pre+=1;
            }
            int init_pre = 0;
            if (map_c.containsKey(s_pre_str)) {
                init_pre = map_c.get(s_pre_str);
            }
            map_c.put(s_pre_str,init_pre+Math.abs(size));
        }

        if (!s_post_l.isEmpty()) {
            for( String post : s_post_l) {
                if (k_post==0) {
                    s_post_str= post;
                } else {
                    s_post_str+= "." + post;
                }
                k_post+=1;
            }
            int init_post = 0;
            if (map_c.containsKey(s_post_str)) {
                init_post = map_c.get(s_post_str);
            }
            map_c.put(s_post_str,init_post-Math.abs(size));
        }
        cap_str = map2string(map_c);
    }

    //This function takes an unknown number of entries : for the unknown number of robots that can be taken into account within a
    //System Net transition.
    public static void inc_c(String... args) {
        int k=0;
        String robot = args[0];

        for (String arg : args) {
            if (k%2 ==0) {
                robot = arg;
            } else {
                inc_c(robot, arg);
            }
            k+=1;
        }
    }

    //However, the Renew Software does not support it, so we are obliged to adapt the entry depending on the number of robots
    public static void inc_c(String robot1, String cond1, String robot2, String cond2) {
        String[] args = new String[] {robot1, cond1, robot2, cond2};
        inc_c(args);
    }

    public static void inc_c(String robot1, String cond1, String robot2, String cond2, String robot3, String cond3) {
        String[] args = new String[] {robot1, cond1,robot2, cond2,robot3, cond3};
        inc_c(args);
    }


    
// Given two solution paths, returns the "best" one. In this version,
// "best" means less steps in the mission net (in a solution, steps are
// in the string separated by means of '\n')
public static String select_best(String oldV, String newV) {
    //Java8: counts the number of '\n' in a string
    long oldVc = oldV.chars().filter(nl -> nl == '\n').count();
    long newVc = newV.chars().filter(nl -> nl == '\n').count();
    if ((oldVc == 0) || (oldVc > newVc)) {//initial case or new is longer
       return newV;
    }
    else {
       return oldV;
    }
 } 

    //User Interface for testing
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
            cap_str=capacity;
            log_str = tup;
            System.out.println(synchro(bu,rob1,cond1,rob2, cond2));
            //System.out.println(inc_t(cond1,cond2));
            //System.out.println(inc_c(rob1,cond1,rob2,cond2));
            System.out.print("bu: ");
            bu = terminalInput.nextLine();
        }
    }
}
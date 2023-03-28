import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower
 * version). This is just a demo, and you can extend and implement functions based on this demo, or
 * implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    System.out.println("read");
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]),
            Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]),
            Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]),
            Double.parseDouble(info[20]),
            Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //1
  public Map<String, Integer> getPtcpCountByInst() {
    Stream<Course> courseStream = courses.stream();
    Map<String, Integer> ret = courseStream.collect(Collectors.groupingBy(Course::getInstitution,
        Collectors.summingInt(Course::getParticipants)));

    return ret;
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Stream<Course> courseStream = courses.stream();
    Map<String, Integer> ret = courseStream.collect(
        Collectors.groupingBy(Course::getInstitutionandSubject,
            Collectors.summingInt(Course::getParticipants)));
    Map<String, Integer> AfterSort = new LinkedHashMap<>();
    ret.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue()
            .reversed()).forEachOrdered(e -> AfterSort.put(e.getKey(), e.getValue()));

    return AfterSort;
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Stream<Course> courseStream = courses.stream();
    Map<String, List<Course>> sig = courseStream.filter(s -> !s.getInstructors().contains(","))
        .collect(Collectors.groupingBy(Course::getInstructors));
    List<String> alltea = new ArrayList<>();
    for (Course course : courses) {
      String []s = course.getInstructors().split(", ");
      for (String s1 : s) {
        if (!alltea.contains(s1)) {
          alltea.add(s1);
        }
      }
    }

    Map<String, List<String>> sig2 = new HashMap<>();
    for (String key : alltea) {
      List<String> list = new ArrayList<>();
      if (!sig.containsKey(key)) {
        sig2.put(key, list);
        continue;
      }
      List<Course> list2 = sig.get(key);
      for (Course course : list2) {
        if (list.contains(course.title)) {
          continue;
        }
        list.add(course.title);
      }
      if (!list.isEmpty()) {
        list.sort(new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            for (int i = 0; i < Math.min(o1.length(), o2.length()); i++) {
              if (o1.charAt(i) > o2.charAt(i)) {
                return 1;
              }
              if (o1.charAt(i) < o2.charAt(i)) {
                return -1;
              }
            }
            if (o1.length() > o2.length()) {
              return 1;
            } else {
              return -1;
            }
          }
        });
      }
      sig2.put(key, list);
    }

    Map<String, List<String>> mul = new HashMap<>();
    for (String key : alltea) {
      List<String> list = new ArrayList<>();
      for (Course course : courses
      ) {
        if (!course.getInstructors().contains(",")) {
          continue;
        }
        String []ss = course.getInstructors().split(", ");
        for (String ss2 : ss) {
          if (ss2.equals(key)) {
            if (!list.contains(course.getTitle())) {
              list.add(course.getTitle());
            }
          }
        }


      }
      list.sort(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          for (int i = 0; i < Math.min(o1.length(), o2.length()); i++) {
            if (o1.charAt(i) > o2.charAt(i)) {
              return 1;
            }
            if (o1.charAt(i) < o2.charAt(i)) {
              return -1;
            }
          }
          if (o1.length() > o2.length()) {
            return 1;
          } else {
            return -1;
          }
        }
      });
      mul.put(key, list);
    }

    Map<String, List<List<String>>> mul2 = new HashMap<>();
    for (String key : alltea) {
      List<String> list1 = sig2.get(key);
      List<String> list2 = mul.get(key);
      List<List<String>> list = new ArrayList<>();
      list.add(list1);
      list.add(list2);
      mul2.put(key, list);
    }

    return mul2;
  }

  //4
  public List<String> getCourses(int topK, String by) {
    List<String> ret = null;
    Stream<Course> courseStream = courses.stream();
    if (Objects.equals(by, "hours")) {
      ret = courseStream.distinct().sorted(Comparator.comparing(Course::getTotalHours).reversed())
          .map(Course::getTitle).distinct()
          .limit(topK).collect(Collectors.toList());
    } else {
      ret = courseStream.sorted(Comparator.comparing(Course::getParticipants).reversed())
          .map(Course::getTitle).distinct()
          .limit(topK).collect(Collectors.toList());
    }
    return ret;
  }

  //5
  public List<String> searchCourses(String courseSubject, double percentAudited,
      double totalCourseHours) {
    Stream<Course> courseStream = courses.stream();
    List<String> ret = courseStream.sorted(Comparator.comparing(Course::getTitle))
        .filter(s -> s.getTotalHours() <= totalCourseHours)
        .filter(s -> s.getPercentAudited() >= percentAudited).filter(
            s -> s.getInstitutionandSubject().toLowerCase().contains(courseSubject.toLowerCase()))
        .map(Course::getTitle).distinct().collect(Collectors.toList());
    return ret;
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {

    Map<String, Double> aMage = courses.stream().collect(
        Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
    Map<String, Double> aMale = courses.stream().collect(Collectors.groupingBy(Course::getNumber,
        Collectors.averagingDouble(Course::getPercentMale)));
    Map<String, Double> aDgree = courses.stream().collect(Collectors.groupingBy(Course::getNumber,
        Collectors.averagingDouble(Course::getPercentDegree)));
    Map<String, Course> Fcourse = courses.stream().collect(
        Collectors.toMap(Course::getNumber, Function.identity(),
            BinaryOperator.maxBy(Comparator.comparingLong(Course::getLaunchDate))));
    List<String> ret = new ArrayList<>();
    Map<String, Double> ran = new HashMap<>();
    List<Double> allran = new ArrayList<>();
    for (String key : aMage.keySet()) {
      double fs;
      double aveage = aMage.get(key);
      double avemal = aMale.get(key);
      double avedeg = aDgree.get(key);

      fs = (0.0 + age - aveage) * (0.0 + age - aveage) + (0.0 + gender * 100 - avemal) * (
          0.0 + gender * 100 - avemal) + (0.0 + isBachelorOrHigher * 100 - avedeg) * (
          0.0 + isBachelorOrHigher * 100 - avedeg);
      fs += 0.000001 * Fcourse.get(key).getTitle().charAt(0);
      ran.put(key, fs);
      allran.add(fs);

    }

    allran.sort(new Comparator<Double>() {
      @Override
      public int compare(Double o1, Double o2) {
        if (o1 < o2) {
          return -1;
        }
        if (o1 > o2) {
          return 1;
        } else {
          String s1 = null;
          String s2 = null;
          for (String key : ran.keySet()) {
            if (o1.equals(ran.get(key))) {
              s1 = key;
            }
            if (o2.equals(ran.get(key))) {
              s2 = key;
            }
          }
          s1 = Fcourse.get(s1).getTitle();
          s2 = Fcourse.get(s2).getTitle();
          System.out.println(s1);
          System.out.println(s2);
          for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) > s2.charAt(i)) {
              return -1;
            }
            if (s1.charAt(i) < s2.charAt(i)) {
              return 1;
            }
          }
          if (s1.length() > s2.length()) {
            return 1;
          } else {
            return -1;
          }
        }
      }
    });
    int maofc = 10;
    for (int i = 0; i < maofc; i++) {
      String num = null;
      double fs = allran.get(i);
      for (String key : ran.keySet()) {
        if (ran.get(key) == fs) {
          num = key;
          Course re = Fcourse.get(num);
          if (ret.contains(re.getTitle())) {
            maofc += 1;
            ran.remove(key);
            break;
          }
          ret.add(re.getTitle());
          ran.remove(key);
          break;
        }
      }
    }
    return ret;
  }

}


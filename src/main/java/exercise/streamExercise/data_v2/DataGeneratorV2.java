package exercise.streamExercise.data_v2;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataGeneratorV2 {
    private static final Random random = new Random();
    private static final ThreadLocalRandom tlr = ThreadLocalRandom.current();

    // Realistic data arrays
    private static final String[] FIRST_NAMES = {
            "Alexander", "Andrew", "Anthony", "Benjamin", "Brian", "Christopher", "Daniel",
            "David", "Edward", "James", "Jason", "John", "Joseph", "Kevin", "Mark",
            "Michael", "Paul", "Richard", "Robert", "Steven", "Thomas", "William",
            "Elizabeth", "Emily", "Jennifer", "Jessica", "Lisa", "Maria", "Mary",
            "Michelle", "Nancy", "Patricia", "Sandra", "Sarah", "Susan"
    };

    private static final String[] LAST_NAMES = {
            "Anderson", "Brown", "Clark", "Davis", "Garcia", "Harris", "Jackson",
            "Johnson", "Jones", "Lee", "Martinez", "Miller", "Moore", "Rodriguez",
            "Smith", "Taylor", "Thomas", "Thompson", "White", "Williams", "Wilson"
    };

    private static final String[] CITIES = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
            "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Seattle",
            "Boston", "Denver", "Washington", "Miami", "Atlanta", "Portland"
    };

    private static final String[] COUNTRIES = {
            "USA", "Canada", "Germany", "France", "UK", "Italy", "Spain", "Netherlands",
            "Sweden", "Norway", "Australia", "Japan", "South Korea"
    };

    private static final String[] UNIVERSITY_NAMES = {
            "Harvard University", "Stanford University", "MIT", "Yale University",
            "Princeton University", "University of Cambridge", "Oxford University",
            "ETH Zurich", "University of Tokyo", "Technical University of Munich"
    };

    private static final String[] FACULTY_NAMES = {
            "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology",
            "Medicine", "Economics", "Law", "Psychology", "Engineering", "Arts",
            "Philosophy", "History", "Literature", "Business Administration"
    };

    private static final String[] COURSE_NAMES = {
            "Data Structures", "Algorithms", "Database Systems", "Machine Learning",
            "Web Development", "Software Engineering", "Calculus", "Linear Algebra",
            "Statistics", "Operating Systems", "Networks", "Artificial Intelligence",
            "Computer Graphics", "Cybersecurity", "Mobile Development"
    };

    private static final String[] SPECIALIZATIONS = {
            "Machine Learning", "Data Science", "Web Development", "Mobile Apps",
            "Cybersecurity", "Cloud Computing", "DevOps", "AI Research",
            "Software Architecture", "Database Design", "UI/UX Design"
    };

    private static final String[] INDUSTRIES = {
            "Technology", "Finance", "Healthcare", "Education", "Manufacturing",
            "Retail", "Consulting", "Media", "Telecommunications", "Energy",
            "Transportation", "Real Estate", "Hospitality", "Agriculture"
    };

    private static final String[] COMPANY_NAMES = {
            "TechCorp", "DataSoft", "InnovateLabs", "CloudSystems", "NextGen Solutions",
            "SmartTech", "DigitalWorks", "FutureSoft", "CodeCraft", "DevHub",
            "TechVision", "ByteForge", "LogicWorks", "SystemsPro", "AppBuilder"
    };

    // Generate complete dataset
    public static UniversityDataset generateCompleteDataset() {
        List<University> universities = generateUniversities(5);
        List<Company> companies = generateCompanies(10);
        
        // Extract all entities from complex structure
        List<Faculty> allFaculties = universities.stream()
                .flatMap(u -> u.getFaculties().stream())
                .collect(Collectors.toList());
                
        List<Course> allCourses = allFaculties.stream()
                .flatMap(f -> f.getCourses().stream())
                .collect(Collectors.toList());
                
        List<Student> allStudents = allCourses.stream()
                .flatMap(c -> c.getEnrolledStudents().stream())
                .distinct() // Remove duplicates
                .collect(Collectors.toList());
                
        List<Teacher> allTeachers = generateTeachers(50, allFaculties);
        List<Grade> allGrades = generateGrades(allStudents, allCourses);
        List<Project> allProjects = generateProjects(30, allStudents, allCourses, companies);

        return new UniversityDataset(universities, allFaculties, allCourses, allStudents,
                                   allTeachers, allGrades, allProjects, companies);
    }

    // Generate universities with nested structure
    public static List<University> generateUniversities(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String name = UNIVERSITY_NAMES[random.nextInt(UNIVERSITY_NAMES.length)];
                    String city = CITIES[random.nextInt(CITIES.length)];
                    String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
                    Integer ranking = 1 + random.nextInt(500);
                    Integer foundedYear = 1800 + random.nextInt(223);
                    Double tuitionFee = 20000 + random.nextDouble() * 60000;
                    
                    List<Faculty> faculties = generateFaculties(3 + random.nextInt(4), (long) i);
                    
                    return new University((long) i, name, city, country, ranking, 
                                        faculties, foundedYear, roundToTwo(tuitionFee));
                })
                .collect(Collectors.toList());
    }

    // Generate faculties with nested courses
    private static List<Faculty> generateFaculties(int count, Long universityId) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String name = FACULTY_NAMES[random.nextInt(FACULTY_NAMES.length)];
                    String dean = getRandomFullName();
                    Integer establishedYear = 1850 + random.nextInt(173);
                    FacultyType type = FacultyType.values()[random.nextInt(FacultyType.values().length)];
                    
                    List<Course> courses = generateCourses(4 + random.nextInt(8), (long) i);
                    
                    return new Faculty((long) i, name, universityId, dean, courses, 
                                     establishedYear, type);
                })
                .collect(Collectors.toList());
    }

    // Generate courses with enrolled students
    private static List<Course> generateCourses(int count, Long facultyId) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String name = COURSE_NAMES[random.nextInt(COURSE_NAMES.length)];
                    String code = generateCourseCode();
                    Integer credits = 2 + random.nextInt(4); // 2-5 credits
                    Integer semester = 1 + random.nextInt(8); // 1-8 semester
                    CourseLevel level = CourseLevel.values()[random.nextInt(CourseLevel.values().length)];
                    Integer maxCapacity = 20 + random.nextInt(81); // 20-100 students
                    Boolean isElective = random.nextDouble() > 0.6; // 40% elective
                    
                    List<Student> enrolledStudents = generateStudents(5 + random.nextInt(25), facultyId);
                    
                    return new Course((long) i, name, code, facultyId, (long) (1 + random.nextInt(20)),
                                    credits, semester, level, enrolledStudents, maxCapacity, isElective);
                })
                .collect(Collectors.toList());
    }

    // Generate students with grades and projects
    public static List<Student> generateStudents(int count, Long facultyId) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                    String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                                 (1000 + i) + "@student.edu";
                    
                    LocalDate birthDate = LocalDate.now()
                            .minusYears(18 + random.nextInt(8)) // 18-25 years old
                            .minusDays(random.nextInt(365));
                            
                    Integer year = 1 + random.nextInt(4); // 1-4 year
                    Double gpa = 2.0 + random.nextDouble() * 2.0; // 2.0-4.0 GPA
                    String city = CITIES[random.nextInt(CITIES.length)];
                    String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
                    StudentStatus status = getRandomStudentStatus();
                    Boolean hasScholarship = random.nextDouble() > 0.7; // 30% have scholarship
                    Double scholarshipAmount = hasScholarship ? 500 + random.nextDouble() * 2500 : 0.0;
                    
                    return new Student((long) i, firstName, lastName, email, birthDate, facultyId,
                                     year, roundToTwo(gpa), city, country, status, new ArrayList<>(),
                                     new ArrayList<>(), hasScholarship, roundToTwo(scholarshipAmount));
                })
                .collect(Collectors.toList());
    }

    // Generate teachers
    public static List<Teacher> generateTeachers(int count, List<Faculty> faculties) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                    String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                    String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@university.edu";
                    
                    Long facultyId = faculties.get(random.nextInt(faculties.size())).getId();
                    AcademicRank rank = AcademicRank.values()[random.nextInt(AcademicRank.values().length)];
                    Double salary = getSalaryByRank(rank);
                    Integer experienceYears = random.nextInt(35) + 1; // 1-35 years
                    
                    List<String> specializations = getRandomSpecializations();
                    LocalDate hireDate = LocalDate.now().minusYears(experienceYears)
                                                       .plusDays(random.nextInt(365));
                    
                    List<Long> courseIds = Arrays.asList((long) (1 + random.nextInt(5)), 
                                                       (long) (1 + random.nextInt(5)));
                    Boolean isTenured = rank.ordinal() >= 2 && random.nextDouble() > 0.4;
                    String officeNumber = (100 + random.nextInt(900)) + Character.toString((char)('A' + random.nextInt(26)));
                    
                    return new Teacher((long) i, firstName, lastName, email, facultyId, rank,
                                     roundToTwo(salary), experienceYears, specializations, hireDate,
                                     courseIds, isTenured, officeNumber);
                })
                .collect(Collectors.toList());
    }

    // Generate grades
    public static List<Grade> generateGrades(List<Student> students, List<Course> courses) {
        List<Grade> grades = new ArrayList<>();
        long gradeId = 1;
        
        for (Student student : students) {
            // Each student has 3-8 grades across different courses
            int gradeCount = 3 + random.nextInt(6);
            Set<Long> usedCourses = new HashSet<>();
            
            for (int i = 0; i < gradeCount; i++) {
                Course course = courses.get(random.nextInt(courses.size()));
                if (usedCourses.contains(course.getId())) continue;
                usedCourses.add(course.getId());
                
                Double score = generateGradeScore();
                GradeType type = GradeType.values()[random.nextInt(GradeType.values().length)];
                LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
                String description = type.toString().toLowerCase().replace('_', ' ');
                Integer attempt = random.nextDouble() > 0.85 ? 2 : 1; // 15% second attempt
                
                grades.add(new Grade(gradeId++, student.getId(), course.getId(),
                                   roundToTwo(score), type, date, description, attempt));
            }
        }
        return grades;
    }

    // Generate projects
    public static List<Project> generateProjects(int count, List<Student> students, 
                                               List<Course> courses, List<Company> companies) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String title = generateProjectTitle();
                    String description = "Project focused on " + title.toLowerCase();
                    
                    // 1-4 students per project
                    int studentCount = 1 + random.nextInt(4);
                    List<Long> studentIds = students.stream()
                            .map(Student::getId)
                            .limit(studentCount)
                            .collect(Collectors.toList());
                    
                    Long courseId = courses.get(random.nextInt(courses.size())).getId();
                    Long companyId = random.nextDouble() > 0.7 ? 
                                   companies.get(random.nextInt(companies.size())).getId() : null;
                    
                    LocalDate startDate = LocalDate.now().minusDays(random.nextInt(180));
                    LocalDate dueDate = startDate.plusDays(30 + random.nextInt(120));
                    
                    ProjectStatus status = ProjectStatus.values()[random.nextInt(ProjectStatus.values().length)];
                    Double budget = random.nextDouble() > 0.5 ? 1000 + random.nextDouble() * 9000 : 0.0;
                    ProjectType type = ProjectType.values()[random.nextInt(ProjectType.values().length)];
                    Integer difficulty = 1 + random.nextInt(10);
                    
                    return new Project((long) i, title, description, studentIds, courseId, companyId,
                                     startDate, dueDate, status, roundToTwo(budget), type, difficulty);
                })
                .collect(Collectors.toList());
    }

    // Generate companies
    public static List<Company> generateCompanies(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    String name = COMPANY_NAMES[random.nextInt(COMPANY_NAMES.length)];
                    String industry = INDUSTRIES[random.nextInt(INDUSTRIES.length)];
                    String city = CITIES[random.nextInt(CITIES.length)];
                    String country = COUNTRIES[random.nextInt(COUNTRIES.length)];
                    CompanySize size = CompanySize.values()[random.nextInt(CompanySize.values().length)];
                    Integer employeeCount = getEmployeeCountBySize(size);
                    Double revenue = employeeCount * (50000 + random.nextDouble() * 150000);
                    Integer foundedYear = 1950 + random.nextInt(73);
                    Boolean isPublic = random.nextDouble() > 0.7;
                    
                    List<Department> departments = generateDepartments(3 + random.nextInt(4), (long) i);
                    
                    return new Company((long) i, name, industry, city, country, size, departments,
                                     employeeCount, roundToTwo(revenue), foundedYear, isPublic);
                })
                .collect(Collectors.toList());
    }

    // Generate departments
    private static List<Department> generateDepartments(int count, Long companyId) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> {
                    DepartmentType type = DepartmentType.values()[random.nextInt(DepartmentType.values().length)];
                    String name = type.toString().toLowerCase().replace('_', ' ');
                    String manager = getRandomFullName();
                    Double budget = 100000 + random.nextDouble() * 900000;
                    Integer headcount = 5 + random.nextInt(46); // 5-50 people
                    String location = CITIES[random.nextInt(CITIES.length)];
                    
                    List<String> employees = IntStream.range(0, headcount)
                            .mapToObj(j -> getRandomFullName())
                            .collect(Collectors.toList());
                    
                    return new Department((long) i, name, companyId, manager, roundToTwo(budget),
                                        employees, type, headcount, location);
                })
                .collect(Collectors.toList());
    }

    // Helper methods
    private static String getRandomFullName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " +
               LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private static String generateCourseCode() {
        return (char)('A' + random.nextInt(26)) + "" + (char)('A' + random.nextInt(26)) +
               (100 + random.nextInt(900));
    }

    private static StudentStatus getRandomStudentStatus() {
        // 80% active, 15% graduate, 5% others
        double rand = random.nextDouble();
        if (rand < 0.8) return StudentStatus.ACTIVE;
        if (rand < 0.95) return StudentStatus.GRADUATE;
        StudentStatus[] others = {StudentStatus.SUSPENDED, StudentStatus.EXCHANGE, StudentStatus.DROPOUT};
        return others[random.nextInt(others.length)];
    }

    private static Double getSalaryByRank(AcademicRank rank) {
        return switch (rank) {
            case ASSISTANT -> 45000 + random.nextDouble() * 15000;
            case LECTURER -> 55000 + random.nextDouble() * 20000;
            case ASSOCIATE_PROFESSOR -> 75000 + random.nextDouble() * 25000;
            case PROFESSOR -> 100000 + random.nextDouble() * 50000;
            case VISITING_PROFESSOR -> 80000 + random.nextDouble() * 40000;
            case EMERITUS_PROFESSOR -> 60000 + random.nextDouble() * 30000;
        };
    }

    private static List<String> getRandomSpecializations() {
        int count = 1 + random.nextInt(3); // 1-3 specializations
        return Arrays.stream(SPECIALIZATIONS)
                .limit(count)
                .collect(Collectors.toList());
    }

    private static Double generateGradeScore() {
        // Normal distribution around 75-85
        double base = 75 + random.nextGaussian() * 10;
        return Math.max(0, Math.min(100, base));
    }

    private static String generateProjectTitle() {
        String[] prefixes = {"Advanced", "Modern", "Smart", "Intelligent", "Automated", "Digital"};
        String[] subjects = {"System", "Platform", "Application", "Framework", "Solution", "Tool"};
        return prefixes[random.nextInt(prefixes.length)] + " " +
               subjects[random.nextInt(subjects.length)];
    }

    private static Integer getEmployeeCountBySize(CompanySize size) {
        return switch (size) {
            case STARTUP -> 10 + random.nextInt(40);
            case SMALL -> 50 + random.nextInt(150);
            case MEDIUM -> 200 + random.nextInt(800);
            case LARGE -> 1000 + random.nextInt(9000);
            case ENTERPRISE -> 10000 + random.nextInt(40000);
        };
    }

    private static Double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
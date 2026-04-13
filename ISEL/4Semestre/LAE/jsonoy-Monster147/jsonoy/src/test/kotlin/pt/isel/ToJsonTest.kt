package pt.isel

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import pt.isel.model.Address
import pt.isel.model.Classroom
import pt.isel.model.Department
import pt.isel.model.Grade
import pt.isel.model.Person
import pt.isel.model.Professor
import pt.isel.model.Student
import pt.isel.model.University
import kotlin.test.Test

class ToJsonTest {
    private val classroom = buildExpectedClassroom()
    private val university = buildExpectedUniversity()
    private val anotherUniversity = buildAnotherUniversity()
    private val gson = Gson()

    @Test
    fun testAddressFieldsToJson() {
        val addr = Address("Oak Avenue", 25, "Porto")
        val json = addr.fieldsToJson().also { println(it) }
        val fromJson = gson.fromJson(json, Address::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(addr)
    }

    @Test
    fun testClassroomFieldsToJson() {
        val json = classroom.fieldsToJson().also { println(it) }
        val fromJson = gson.fromJson(json, Classroom::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(classroom)
    }

    @Test
    fun testClassroomMembersToJson() {
        val json = classroom.membersToJson().also { println(it) }
        val fromJson = gson.fromJson(json, Classroom::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(classroom)
    }

    @Test
    fun testPersonMembersToJson() {
        val alice = Person(id = 1, name = "Alice", born = 946684800000L) // Born: Jan 1, 2000
        val json = alice.membersToJson().also { println(it) }
        val fromJson = gsonPerson.fromJson(json, Person::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(alice)
    }

    private fun buildExpectedClassroom(): Classroom =
        Classroom(
            id = "C1",
            students =
                listOf(
                    Student(
                        "Alice",
                        1,
                        "Portugal",
                        Address("Main Street", 10, "Lisbon"),
                        listOf(Grade("Math", 18), Grade("Science", 17)),
                    ),
                    Student(
                        "Bob",
                        2,
                        "Spain",
                        Address("Rose Street", 17, "Lisbon"),
                        listOf(Grade("History", 14), Grade("English", 16)),
                    ),
                    Student("Charlie", 3, "Portugal", Address("Sunset Blvd", 22, "Porto")),
                ),
        )

    @Test
    fun testUniversityMembersToJson() {
        val json = university.membersToJson().also { println(it) }
        val fromJson = gson.fromJson(json, University::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(university)
    }

    @Test
    fun testAnotherUniversityMembersToJson() {
        val json = anotherUniversity.membersToJson().also { println(it) }
        val fromJson = gson.fromJson(json, University::class.java)
        assertThat(fromJson).usingRecursiveComparison().isEqualTo(anotherUniversity)
    }

    @Test
    fun testUniversityMembersToJsonWithFunction() {
        val university = buildNewUniversity()
        val expectedJson =
            """
            {
                "departments": [
                    {
                        "name": "Engineering",
                        "professors": [
                            {"employeeId": 301, "name": "Dr. Eve", "specialization": "Mechanical"},
                            {"employeeId": 302, "name": "Dr. Frank", "specialization": "Electrical"}
                        ]
                    },
                    {
                        "name": "Arts",
                        "professors": [
                            {"employeeId": 401, "name": "Dr. Grace", "specialization": "Painting"},
                            {"employeeId": 402, "name": "Dr. Heidi", "specialization": "Sculpture"}
                        ]
                    }
                ],
                "name": "New University",
                "getDepartmentCount": 2
            }
            """.trimIndent().replace("\n", "").replace(" ", "")
        val actualJson = university.membersToJson().replace("\n", "").replace(" ", "")
        assertThat(actualJson).isEqualTo(expectedJson)
    }

    private fun buildExpectedUniversity(): University =
        University(
            name = "Tech University",
            departments =
                listOf(
                    Department(
                        name = "Computer Science",
                        professors =
                            listOf(
                                Professor(name = "Dr. Alice", employeeId = 101, specialization = "AI"),
                                Professor(name = "Dr. Bob", employeeId = 102, specialization = "Cybersecurity"),
                            ),
                    ),
                    Department(
                        name = "Mathematics",
                        professors =
                            listOf(
                                Professor(name = "Dr. Charlie", employeeId = 201, specialization = "Algebra"),
                                Professor(name = "Dr. Dave", employeeId = 202, specialization = "Statistics"),
                            ),
                    ),
                ),
        )

    private fun buildAnotherUniversity(): University =
        University(
            name = "Science University",
            departments =
                listOf(
                    Department(
                        name = "Physics",
                        professors =
                            listOf(
                                Professor(name = "Dr. Eve", employeeId = 301, specialization = "Quantum Mechanics"),
                                Professor(name = "Dr. Frank", employeeId = 302, specialization = "Astrophysics"),
                            ),
                    ),
                    Department(
                        name = "Chemistry",
                        professors =
                            listOf(
                                Professor(name = "Dr. Grace", employeeId = 401, specialization = "Organic Chemistry"),
                                Professor(name = "Dr. Heidi", employeeId = 402, specialization = "Inorganic Chemistry"),
                            ),
                    ),
                ),
        )

    private fun buildNewUniversity(): University =
        University(
            name = "New University",
            departments =
                listOf(
                    Department(
                        name = "Engineering",
                        professors =
                            listOf(
                                Professor(name = "Dr. Eve", employeeId = 301, specialization = "Mechanical"),
                                Professor(name = "Dr. Frank", employeeId = 302, specialization = "Electrical"),
                            ),
                    ),
                    Department(
                        name = "Arts",
                        professors =
                            listOf(
                                Professor(name = "Dr. Grace", employeeId = 401, specialization = "Painting"),
                                Professor(name = "Dr. Heidi", employeeId = 402, specialization = "Sculpture"),
                            ),
                    ),
                ),
        )
}

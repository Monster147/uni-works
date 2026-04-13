package pt.isel.model

class University(
    val name: String,
    val departments: List<Department>,
) {
    fun getDepartmentCount() = departments.size
}

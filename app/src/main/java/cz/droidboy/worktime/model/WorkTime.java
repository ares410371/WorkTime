package cz.droidboy.worktime.model;

import org.joda.time.DateTime;

/**
 * @author Jonas Sevcik
 */
public final class WorkTime {

    private Long id;
    private DateTime startDate;
    private DateTime endDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkTime workTime = (WorkTime) o;

        if (id != null ? !id.equals(workTime.id) : workTime.id != null) return false;
        if (!startDate.equals(workTime.startDate)) return false;
        return !(endDate != null ? !endDate.equals(workTime.endDate) : workTime.endDate != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + startDate.hashCode();
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WorkTime{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}

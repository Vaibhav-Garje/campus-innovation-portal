import mysql.connector

def check_db():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="1234",
            database="campusportal"
        )
        print("SUCCESS: Connected to campusportal")
        conn.close()
    except mysql.connector.Error as err:
        print(f"ERROR: {err}")

if __name__ == "__main__":
    check_db()

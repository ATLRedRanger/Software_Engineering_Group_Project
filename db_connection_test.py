import mysql.connector

config = {
    "host": "localhost",
    "user": "root",
    "password": "Sunshine99@@",
    "database": "playfair_db"
}

try:
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()

    cursor.execute("SELECT id, cipher_key, omitted_letter, grid_string FROM cipher_grids")

    print(" Connected to Database:\n")
    for row in cursor.fetchall():
        print(row)

except mysql.connector.Error as err:
    print("Database connection failed")
    print(err)

finally:
    if 'cursor' in locals():
        cursor.close()
    if 'conn' in locals():
        conn.close()
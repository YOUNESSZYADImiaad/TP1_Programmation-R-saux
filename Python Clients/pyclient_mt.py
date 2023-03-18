import socket
import threading


class ClientThread(threading.Thread):
    def __init__(self, client_socket):
        threading.Thread.__init__(self)
        self.client_socket = client_socket

    def run(self):
        while True:
            response = self.client_socket.recv(1024)
            print(response.decode())


# create client socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# connect to server
server_address = ('localhost', 1234)
client_socket.connect(server_address)

# start client thread to receive messages from server
client_thread = ClientThread(client_socket)
client_thread.start()

# send messages to server
while True:
    message = input("")
    client_socket.sendall((message + "\n").encode())

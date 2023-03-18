import socket
import threading


class PythonClient:
    def __init__(self, host, port):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((host, port))

        self.is_running = True
        self.response_thread = threading.Thread(
            target=self.listen_for_responses)
        self.response_thread.start()

    def listen_for_responses(self):
        while self.is_running:
            try:
                response = self.socket.recv(1024).decode()
                print(response)
            except:
                break

    def send_request(self, message):
        self.socket.send(message.encode())

    def stop(self):
        self.is_running = False
        self.socket.close()


client = PythonClient('localhost', 1234)

while True:
    req = input('=>')
    client.send_request(req)

    if req == 'quit':
        client.stop()
        break

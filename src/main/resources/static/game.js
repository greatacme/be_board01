class JanggiBoard {
    constructor() {
        this.canvas = document.getElementById('board');
        this.ctx = this.canvas.getContext('2d');

        // Board dimensions
        this.cols = 9; // 9 vertical lines (0-8)
        this.rows = 10; // 10 horizontal lines (0-9)
        this.cellSize = 70;
        this.padding = 40;

        // Adjust canvas size
        this.canvas.width = this.cellSize * (this.cols - 1) + this.padding * 2;
        this.canvas.height = this.cellSize * (this.rows - 1) + this.padding * 2;

        // Game state
        this.pieces = [];
        this.selectedPiece = null;
        this.currentTurn = 'RED';
        this.roomId = null;
        this.sessionId = this.generateSessionId();
        this.playerId = this.generatePlayerId();
        this.pollingInterval = null;
        this.gameStatus = 'WAITING';

        // Initialize
        this.initializeBoard();
        this.setupEventListeners();
        this.drawBoard();
        this.drawPieces();
        this.updateIdDisplay();
    }

    generateSessionId() {
        return 'S' + Math.random().toString(36).substring(2, 9).toUpperCase();
    }

    generatePlayerId() {
        return 'P' + Math.random().toString(36).substring(2, 9).toUpperCase();
    }

    updateIdDisplay() {
        document.getElementById('session-id').textContent = `접속 ID: ${this.sessionId}`;
        document.getElementById('player-id').textContent = `플레이어 ID: ${this.playerId}`;
        document.getElementById('room-id').textContent = this.roomId || '-';
    }

    initializeBoard() {
        // Initialize BLUE pieces (top, y: 0-3)
        let bluePieceCount = 0;
        for (let y = 0; y <= 3; y++) {
            for (let x = 0; x <= 8; x++) {
                this.pieces.push({
                    id: 'B' + (++bluePieceCount),
                    color: 'BLUE',
                    x: x,
                    y: y,
                    captured: false
                });
            }
        }

        // Initialize RED pieces (bottom, y: 6-9)
        let redPieceCount = 0;
        for (let y = 6; y <= 9; y++) {
            for (let x = 0; x <= 8; x++) {
                this.pieces.push({
                    id: 'R' + (++redPieceCount),
                    color: 'RED',
                    x: x,
                    y: y,
                    captured: false
                });
            }
        }
    }

    drawBoard() {
        const ctx = this.ctx;

        // Clear canvas with white background
        ctx.fillStyle = 'white';
        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        ctx.strokeStyle = '#000';
        ctx.lineWidth = 1.5;

        // Draw horizontal lines
        for (let i = 0; i < this.rows; i++) {
            const y = this.padding + i * this.cellSize;
            ctx.beginPath();
            ctx.moveTo(this.padding, y);
            ctx.lineTo(this.padding + (this.cols - 1) * this.cellSize, y);
            ctx.stroke();
        }

        // Draw vertical lines
        for (let i = 0; i < this.cols; i++) {
            const x = this.padding + i * this.cellSize;
            ctx.beginPath();
            ctx.moveTo(x, this.padding);
            ctx.lineTo(x, this.padding + (this.rows - 1) * this.cellSize);
            ctx.stroke();
        }

        // Draw palace diagonals
        this.drawPalaceDiagonals();

        // Draw river label
        this.drawRiverLabel();
    }

    drawPalaceDiagonals() {
        const ctx = this.ctx;
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 1.5;

        // Blue palace (top) - x: 3-5, y: 0-2
        const bluePalaceX1 = this.padding + 3 * this.cellSize;
        const bluePalaceX2 = this.padding + 5 * this.cellSize;
        const bluePalaceY1 = this.padding + 0 * this.cellSize;
        const bluePalaceY2 = this.padding + 2 * this.cellSize;

        ctx.beginPath();
        ctx.moveTo(bluePalaceX1, bluePalaceY1);
        ctx.lineTo(bluePalaceX2, bluePalaceY2);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(bluePalaceX2, bluePalaceY1);
        ctx.lineTo(bluePalaceX1, bluePalaceY2);
        ctx.stroke();

        // Red palace (bottom) - x: 3-5, y: 7-9
        const redPalaceX1 = this.padding + 3 * this.cellSize;
        const redPalaceX2 = this.padding + 5 * this.cellSize;
        const redPalaceY1 = this.padding + 7 * this.cellSize;
        const redPalaceY2 = this.padding + 9 * this.cellSize;

        ctx.beginPath();
        ctx.moveTo(redPalaceX1, redPalaceY1);
        ctx.lineTo(redPalaceX2, redPalaceY2);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(redPalaceX2, redPalaceY1);
        ctx.lineTo(redPalaceX1, redPalaceY2);
        ctx.stroke();
    }

    drawRiverLabel() {
        const ctx = this.ctx;
        ctx.font = '16px serif';
        ctx.fillStyle = '#666';
        ctx.textAlign = 'center';

        const riverY = this.padding + 4.5 * this.cellSize;
        ctx.fillText('楚 河', this.padding + 2 * this.cellSize, riverY);
        ctx.fillText('漢 界', this.padding + 6 * this.cellSize, riverY);
    }

    drawPieces() {
        const ctx = this.ctx;

        this.pieces.forEach(piece => {
            if (piece.captured) return;

            const x = this.padding + piece.x * this.cellSize;
            const y = this.padding + piece.y * this.cellSize;

            // Draw pentagon (Japanese shogi-style piece) facing each other
            ctx.beginPath();
            if (piece.color === 'RED') {
                // RED: pointing right (towards opponent)
                ctx.moveTo(x - 18, y - 20);        // Left top
                ctx.lineTo(x - 18, y + 20);        // Left bottom
                ctx.lineTo(x + 12, y + 14);        // Bottom right
                ctx.lineTo(x + 20, y);             // Right (pointed)
                ctx.lineTo(x + 12, y - 14);        // Top right
            } else {
                // BLUE: pointing left (towards opponent)
                ctx.moveTo(x + 18, y - 20);        // Right top
                ctx.lineTo(x + 18, y + 20);        // Right bottom
                ctx.lineTo(x - 12, y + 14);        // Bottom left
                ctx.lineTo(x - 20, y);             // Left (pointed)
                ctx.lineTo(x - 12, y - 14);        // Top left
            }
            ctx.closePath();

            // Fill color based on piece color
            if (piece.color === 'RED') {
                ctx.fillStyle = '#d32f2f'; // Red
            } else {
                ctx.fillStyle = '#2196F3'; // Blue
            }
            ctx.fill();

            // Draw border
            ctx.strokeStyle = '#000';
            ctx.lineWidth = 2;
            ctx.stroke();

            // Draw piece symbol and name
            if (piece.type) {
                ctx.textAlign = 'center';
                ctx.fillStyle = '#FFFFFF'; // White text
                ctx.strokeStyle = '#000'; // Black outline for text

                // Draw symbol (top, larger)
                if (piece.type.symbol) {
                    ctx.font = 'bold 16px sans-serif';
                    ctx.textBaseline = 'middle';
                    ctx.lineWidth = 0.5;
                    ctx.strokeText(piece.type.symbol, x, y - 6);
                    ctx.fillText(piece.type.symbol, x, y - 6);
                }

                // Draw korean name (bottom, smaller)
                if (piece.type.koreanName) {
                    ctx.font = 'bold 8px sans-serif';
                    ctx.textBaseline = 'top';
                    ctx.lineWidth = 0.3;
                    ctx.strokeText(piece.type.koreanName, x, y + 6);
                    ctx.fillText(piece.type.koreanName, x, y + 6);
                }
            }

            // Highlight selected piece
            if (this.selectedPiece &&
                this.selectedPiece.x === piece.x &&
                this.selectedPiece.y === piece.y) {
                ctx.beginPath();
                if (piece.color === 'RED') {
                    ctx.moveTo(x - 22, y - 24);
                    ctx.lineTo(x - 22, y + 24);
                    ctx.lineTo(x + 16, y + 18);
                    ctx.lineTo(x + 24, y);
                    ctx.lineTo(x + 16, y - 18);
                } else {
                    ctx.moveTo(x + 22, y - 24);
                    ctx.lineTo(x + 22, y + 24);
                    ctx.lineTo(x - 16, y + 18);
                    ctx.lineTo(x - 24, y);
                    ctx.lineTo(x - 16, y - 18);
                }
                ctx.closePath();
                ctx.strokeStyle = '#00ff00';
                ctx.lineWidth = 3;
                ctx.stroke();
            }
        });
    }

    setupEventListeners() {
        this.canvas.addEventListener('click', (e) => this.handleClick(e));

        document.getElementById('new-game').addEventListener('click', () => {
            this.createNewGame();
        });

        document.getElementById('join-game').addEventListener('click', () => {
            this.showJoinRoomDialog();
        });

        document.getElementById('join-room-submit').addEventListener('click', () => {
            const roomId = document.getElementById('join-room-id').value.trim();
            if (roomId) {
                this.joinRoom(roomId);
            }
        });

        document.getElementById('join-room-cancel').addEventListener('click', () => {
            this.hideJoinRoomDialog();
        });

        document.getElementById('reset').addEventListener('click', () => {
            this.reset();
        });
    }

    showJoinRoomDialog() {
        document.getElementById('join-room-container').style.display = 'flex';
        document.getElementById('join-room-id').value = '';
        document.getElementById('join-room-id').focus();
    }

    hideJoinRoomDialog() {
        document.getElementById('join-room-container').style.display = 'none';
    }

    joinRoom(roomId) {
        console.log('Joining room:', roomId, 'with playerId:', this.playerId);
        fetch(`/api/game/rooms/${roomId}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                playerId: this.playerId
            })
        })
        .then(response => {
            console.log('Join room response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('Join room response:', data);
            if (data.roomId) {
                this.roomId = data.roomId;
                this.loadGameState(data);
                this.updateIdDisplay();
                this.hideJoinRoomDialog();
                this.startPolling();
                console.log('Joined room, polling started. Room ID:', this.roomId);
                alert(`방에 참여했습니다! (${data.playerColor})`);
            } else {
                alert(data.message || '방 참여에 실패했습니다.');
            }
        })
        .catch(err => {
            console.error('Join room error:', err);
            alert('방 참여 중 오류가 발생했습니다: ' + err.message);
        });
    }

    handleClick(e) {
        const rect = this.canvas.getBoundingClientRect();
        const clickX = e.clientX - rect.left;
        const clickY = e.clientY - rect.top;

        // Convert to grid coordinates
        const gridX = Math.round((clickX - this.padding) / this.cellSize);
        const gridY = Math.round((clickY - this.padding) / this.cellSize);

        // Check if click is valid
        if (gridX < 0 || gridX >= this.cols || gridY < 0 || gridY >= this.rows) {
            return;
        }

        // Find piece at clicked position
        const clickedPiece = this.getPieceAt(gridX, gridY);

        if (this.selectedPiece) {
            // Try to move
            if (clickedPiece && clickedPiece.color === this.selectedPiece.color) {
                // Select different piece of same color
                this.selectedPiece = clickedPiece;
            } else {
                // Try to move
                this.movePiece(this.selectedPiece, gridX, gridY);
            }
        } else {
            // Select piece
            if (clickedPiece && clickedPiece.color === this.currentTurn) {
                this.selectedPiece = clickedPiece;
            }
        }

        this.redraw();
    }

    getPieceAt(x, y) {
        return this.pieces.find(p =>
            !p.captured && p.x === x && p.y === y
        );
    }

    movePiece(piece, toX, toY) {
        // Send move request to server
        const moveRequest = {
            roomId: this.roomId,
            from: { x: piece.x, y: piece.y },
            to: { x: toX, y: toY }
        };

        fetch('/api/game/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(moveRequest)
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'MOVE_SUCCESS') {
                // Update local state
                const targetPiece = this.getPieceAt(toX, toY);
                if (targetPiece) {
                    targetPiece.captured = true;
                }
                piece.x = toX;
                piece.y = toY;
                this.selectedPiece = null;
                this.currentTurn = data.currentTurn;
                this.updateTurnDisplay();

                if (data.gameOver) {
                    alert(`게임 종료! ${data.winner === 'RED' ? '홍' : '초'}팀 승리!`);
                }
            } else {
                alert('잘못된 이동입니다.');
                this.selectedPiece = null;
            }
            this.redraw();
        })
        .catch(err => {
            console.error('Move error:', err);
            this.selectedPiece = null;
            this.redraw();
        });
    }

    createNewGame() {
        console.log('Creating new game with playerId:', this.playerId);
        fetch('/api/game/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                playerId: this.playerId
            })
        })
        .then(response => {
            console.log('Create game response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('Game created:', data);
            this.roomId = data.roomId;
            this.loadGameState(data);
            this.updateIdDisplay();
            this.startPolling();
            console.log('Game created, polling started. Room ID:', this.roomId);
        })
        .catch(err => {
            console.error('Create game error:', err);
            alert('게임 생성 실패: ' + err.message);
        });
    }

    startPolling() {
        console.log('Starting polling for room:', this.roomId);
        if (this.pollingInterval) {
            console.log('Clearing existing polling interval');
            clearInterval(this.pollingInterval);
        }

        // Poll immediately once
        if (this.roomId) {
            console.log('Polling immediately...');
            this.pollGameState();
        }

        this.pollingInterval = setInterval(() => {
            if (this.roomId) {
                console.log('Polling interval tick, room:', this.roomId);
                this.pollGameState();
            }
        }, 1000); // Poll every second
        console.log('Polling interval set:', this.pollingInterval);
    }

    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
    }

    pollGameState() {
        fetch(`/api/game/rooms/${this.roomId}`)
            .then(response => response.json())
            .then(data => {
                const oldStatus = this.gameStatus;
                this.gameStatus = data.status;

                console.log('Poll result:', {
                    oldStatus,
                    newStatus: data.status,
                    redPlayer: data.redPlayer,
                    bluePlayer: data.bluePlayer,
                    redReady: data.redPlayerReady,
                    blueReady: data.bluePlayerReady
                });

                // Check if we need to transition to SETUP mode
                if (oldStatus === 'WAITING' && data.status === 'SETUP') {
                    console.log('Transitioning to SETUP mode');
                    this.handleSetupMode(data);
                }

                // Check if both players are ready and game is starting
                if (data.status === 'PLAYING' && oldStatus === 'SETUP') {
                    console.log('Game starting!');
                    this.handleGameStart(data);
                }

                // Update player ready status display
                this.updatePlayerReadyStatus(data);
            })
            .catch(err => console.error('Poll error:', err));
    }

    handleSetupMode(data) {
        alert('두 플레이어가 접속했습니다! 말 배치를 시작하세요.');
        // Here you would implement piece placement UI
    }

    handleGameStart(data) {
        alert('게임 시작!');
        this.loadGameState(data);
    }

    updatePlayerReadyStatus(data) {
        let statusText = '';

        if (data.status === 'WAITING') {
            const playerCount = (data.redPlayer ? 1 : 0) + (data.bluePlayer ? 1 : 0);
            statusText = `플레이어 대기 중... (${playerCount}/2)`;
        } else if (data.status === 'SETUP') {
            const redReady = data.redPlayerReady ? '✓' : '✗';
            const blueReady = data.bluePlayerReady ? '✓' : '✗';
            statusText = `준비 상태 - RED: ${redReady} BLUE: ${blueReady}`;
        } else if (data.status === 'PLAYING') {
            statusText = '게임 진행 중';
        }

        console.log('Updating status text to:', statusText);
        const turnElement = document.getElementById('turn');
        console.log('Turn element:', turnElement);
        if (turnElement) {
            turnElement.textContent = statusText;
            console.log('Status text updated successfully');
        } else {
            console.error('Turn element not found!');
        }
    }

    loadGameState(data) {
        this.pieces = data.pieces;
        this.currentTurn = data.currentTurn;
        this.gameStatus = data.status;
        this.selectedPiece = null;
        this.updateTurnDisplay();
        this.updatePlayerReadyStatus(data);
        this.redraw();
    }

    reset() {
        this.stopPolling();
        this.pieces = [];
        this.selectedPiece = null;
        this.currentTurn = 'RED';
        this.roomId = null;
        this.gameStatus = 'WAITING';
        this.initializeBoard();
        this.updateTurnDisplay();
        this.updateIdDisplay();
        this.redraw();
        document.getElementById('turn').textContent = '차례: 홍';
    }

    updateTurnDisplay() {
        const turnSpan = document.getElementById('current-turn');
        turnSpan.textContent = this.currentTurn === 'RED' ? '홍' : '초';
        turnSpan.style.color = this.currentTurn === 'RED' ? '#d32f2f' : '#2196F3';
    }

    redraw() {
        this.drawBoard();
        this.drawPieces();
    }
}

// Initialize game when page loads
window.addEventListener('load', () => {
    const game = new JanggiBoard();
});
